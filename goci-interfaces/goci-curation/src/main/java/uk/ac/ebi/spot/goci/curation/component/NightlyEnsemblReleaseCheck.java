package uk.ac.ebi.spot.goci.curation.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.ebi.spot.goci.component.EnsemblDbsnpVersion;
import uk.ac.ebi.spot.goci.component.EnsemblGenomeBuildVersion;
import uk.ac.ebi.spot.goci.component.EnsemblRelease;
import uk.ac.ebi.spot.goci.curation.service.MailService;
import uk.ac.ebi.spot.goci.model.MappingMetadata;
import uk.ac.ebi.spot.goci.repository.MappingMetadataRepository;
import uk.ac.ebi.spot.goci.service.MappingService;

import java.util.Date;
import java.util.List;

/**
 * Created by emma on 28/09/2015.
 *
 * @author emma
 *         <p>
 *         Scheduled component that runs nightly and pings Ensembl REST API to check for changes in release number.
 */
@Component
public class NightlyEnsemblReleaseCheck {

    private EnsemblRelease ensemblRelease;

    private EnsemblGenomeBuildVersion ensemblGenomeBuildVersion;

    private EnsemblDbsnpVersion ensemblDbsnpVersion;

    private MappingMetadataRepository mappingMetadataRepository;

    private MailService mailService;

    private MappingService mappingService;

    @Autowired
    public NightlyEnsemblReleaseCheck(EnsemblRelease ensemblRelease,
                                      EnsemblGenomeBuildVersion ensemblGenomeBuildVersion,
                                      EnsemblDbsnpVersion ensemblDbsnpVersion,
                                      MailService mailService,
                                      MappingService mappingService,
                                      MappingMetadataRepository mappingMetadataRepository) {
        this.ensemblRelease = ensemblRelease;
        this.ensemblGenomeBuildVersion = ensemblGenomeBuildVersion;
        this.ensemblDbsnpVersion = ensemblDbsnpVersion;
        this.mailService = mailService;
        this.mappingService = mappingService;
        this.mappingMetadataRepository = mappingMetadataRepository;
    }

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected Logger getLog() {
        return log;
    }

    /**
     * Method used to determine if there has been a new Ensembl release
     */
    @Scheduled(cron = "0 00 22 ? * MON-FRI")
    public void checkRelease() {

        // Get relevant metadata
        int latestEnsemblReleaseNumber = ensemblRelease.getReleaseVersion();
        String genomeBuildVersion = ensemblGenomeBuildVersion.getGenomeBuildVersion();
        int dbsnpVersion = ensemblDbsnpVersion.getDbsnpVersion();

        String performer = "Release " + latestEnsemblReleaseNumber + " mapping";

        List<MappingMetadata> mappingMetadataList = mappingMetadataRepository.findAll(sortByUsageStartDateDesc());

        // If there are no details in table then add them
        if (mappingMetadataList.isEmpty()) {
            getLog().info("No mapping metadata found, adding information to database and mapping data to release " +
                                  latestEnsemblReleaseNumber);
            createMappingMetaData(latestEnsemblReleaseNumber, genomeBuildVersion, dbsnpVersion);

            // Send email
            mailService.sendReleaseChangeEmail(null,
                                               latestEnsemblReleaseNumber);

            // Map database contents
            mappingService.mapCatalogContents(performer);
        }
        else {
            Integer currentEnsemblReleaseNumberInDatabase = mappingMetadataList.get(0).getEnsemblReleaseNumber();

            // If the latest release in database does not match
            // the latest Ensembl release do mapping and send notification email
            if (!currentEnsemblReleaseNumberInDatabase.equals(latestEnsemblReleaseNumber)) {
                if (currentEnsemblReleaseNumberInDatabase < latestEnsemblReleaseNumber) {

                    // Create new entry in mapping_metadata table
                    createMappingMetaData(latestEnsemblReleaseNumber, genomeBuildVersion, dbsnpVersion);

                    // Send email
                    mailService.sendReleaseChangeEmail(currentEnsemblReleaseNumberInDatabase,
                                                       latestEnsemblReleaseNumber);

                    // Perform remapping and set performer
                    getLog().info("New Ensembl release identified: " + latestEnsemblReleaseNumber);
                    getLog().info("Remapping all database contents");
                    mappingService.mapCatalogContents(performer);
                }
                else {
                    getLog().error("Ensembl Release Integrity Issue: Current Ensembl release is " +
                                           latestEnsemblReleaseNumber +
                                           ". Database release number is set to " +
                                           currentEnsemblReleaseNumberInDatabase);
                }
            }
            else {
                getLog().info("Current Ensembl release is " + latestEnsemblReleaseNumber +
                                      ", the current release used to map database is " +
                                      currentEnsemblReleaseNumberInDatabase);
            }
        }
    }

    /**
     * Method used to create and save new Ensembl release details in the database
     *
     * @param ensemblReleaseNumber the latest Ensembl release number returned from Ensembl API
     * @param genomeBuildVersion   the latest Genome build version returned from Ensembl API
     * @param dbsnpVersion         the latest dbSNP version returned from Ensembl API
     */
    private void createMappingMetaData(int ensemblReleaseNumber,
                                       String genomeBuildVersion,
                                       int dbsnpVersion) {
        MappingMetadata newMappingMetadata = new MappingMetadata();
        newMappingMetadata.setEnsemblReleaseNumber(ensemblReleaseNumber);
        newMappingMetadata.setGenomeBuildVersion(genomeBuildVersion);
        newMappingMetadata.setDbsnpVersion(dbsnpVersion);
        newMappingMetadata.setUsageStartDate(new Date());
        mappingMetadataRepository.save(newMappingMetadata);
    }

    /**
     * Method used to create a sorting option for a database query
     *
     * @return Sort object used by database query
     */
    private Sort sortByUsageStartDateDesc() {return new Sort(new Sort.Order(Sort.Direction.DESC, "usageStartDate"));}
}
