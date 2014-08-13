package uk.ac.ebi.fgpt.goci.pussycat.renderlet;

import net.sourceforge.fluxion.spi.ServiceProvider;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.fgpt.goci.lang.OntologyConstants;
import uk.ac.ebi.fgpt.goci.pussycat.layout.BandInformation;
import uk.ac.ebi.fgpt.goci.pussycat.layout.SVGArea;
import uk.ac.ebi.fgpt.goci.pussycat.layout.SVGCanvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderlet that can generate SVG for OWLIndividuals representing GWAS trait associations
 *
 * @author dwelter
 * @date 18/04/12
 */
@ServiceProvider
public class AssociationRenderlet implements Renderlet<OWLReasoner, OWLNamedIndividual> {
    private final Map<OWLReasoner, Map<BandInformation, BandInformation>> previousBandMapByReasoner;
    private final Map<BandInformation, SVGArea> renderedBands;

    private Logger log = LoggerFactory.getLogger(getClass());

    public AssociationRenderlet() {
        this.previousBandMapByReasoner = new HashMap<OWLReasoner, Map<BandInformation, BandInformation>>();
        this.renderedBands = new HashMap<BandInformation, SVGArea>();
    }

    protected Logger getLog() {
        return log;
    }

    @Override
    public String getName() {
        return "Association renderlet";
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

    @Override
    public String getDescription() {
        return ("Renderlet capable of creating GWAS trait-SNP association visualisations");
    }

    @Override
    public boolean canRender(RenderletNexus nexus, Object renderingContext, Object renderingEntity) {
        if (renderingContext instanceof OWLReasoner) {
            if (renderingEntity instanceof OWLNamedIndividual) {
                OWLOntology ontology = ((OWLReasoner) renderingContext).getRootOntology();
                OWLNamedIndividual individual = (OWLNamedIndividual) renderingEntity;
                if (nexus.getLocationOfRenderedEntity(individual) == null) {
                    for (OWLClassExpression type : individual.getTypes(ontology)) {
                        OWLClass typeClass = type.asOWLClass();
                        if (typeClass.getIRI().equals(IRI.create(OntologyConstants.TRAIT_ASSOCIATION_CLASS_IRI))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void render(RenderletNexus nexus, OWLReasoner reasoner, OWLNamedIndividual individual) {
        getLog().trace("Association: " + individual);

        OWLOntology ontology = reasoner.getRootOntology();
        if (!previousBandMapByReasoner.containsKey(reasoner)) {
            sortBandsWithData(reasoner);
        }

        BandInformation band = getBandInformation(individual, ontology);
        SVGArea location = nexus.getLocationOfRenderedEntity(band);

        BandInformation previousBand = null;
        SVGArea previousLocation = null;
        Map<BandInformation, BandInformation> previousBandMap = previousBandMapByReasoner.get(reasoner);
        if (previousBandMap != null) {
            previousBand = previousBandMap.get(band);
            previousLocation = nexus.getLocationOfRenderedEntity(previousBand);
        }

        if (band != null) {
            StringBuilder svg = new StringBuilder();

            //there is no other association in this chromosmal band yet - render
            if (!renderedBands.containsKey(band)) {
                getLog().trace("First association for this band");

                String transform = chromosomeTransform(band.getChromosome());
                svg.append("<g ")
                        .append("id='").append(individual.getIRI().toString()).append("' ")
                        .append("transform='").append(transform).append("' ")
                        .append("class='gwas-trait'>");

                if (location != null) {
                    double x = location.getX();
                    double y = location.getY();
                    double width = location.getWidth();
                    double height = location.getHeight();
                    double newY = y + (height / 2);
                    double endY = newY;
                    double length = 1.75 * width;
                    double newHeight = 0;

                    // fanning algorithm
                    Set<OWLNamedIndividual> traits = getAllTraitsForAssociation(reasoner, individual);
                    if (previousLocation != null) {
                        double prevY = previousLocation.getY();
                        double prevHeight = previousLocation.getHeight(); // todo - I think?
                        double radius = 0.35 * width;

                        if (band.getBandName().contains("p")) {
                            int drop = ((traits.size() - 1) / 6) + 2;
                            double min = prevY - (drop * radius);
                            if (min <= newY) {
                                endY = min;
                                newHeight = endY - newY;
                            }
                        }
                        else {
//                        int drop = ((previous.getTraitNames().size() - 1) / 6) + 2;
//                        double min = prevY + (drop * radius);
                            double min = prevY + prevHeight;
                            if (min >= newY) {
                                endY = min;
                                newHeight = endY - newY;
                            }
                        }
                    }

                    StringBuilder d = new StringBuilder();
                    if (previousLocation == null || newHeight == 0) {
                        d.append("m ");
                        d.append(Double.toString(x));
                        d.append(",");
                        d.append(Double.toString(newY));
                        d.append(" ");
                        d.append(Double.toString(length));
                        d.append(",0.0");
                    }

                    else {
                        double width2 = 0.75 * width;
                        d.append("m ");
                        d.append(Double.toString(x));
                        d.append(",");
                        d.append(Double.toString(newY));
                        d.append(" ");
                        d.append(Double.toString(width));
                        d.append(",0.0, ");
                        d.append(Double.toString(width2));
                        d.append(",");
                        d.append(Double.toString(newHeight));
                    }

                    svg.append("<path ")
                            .append("d='").append(d.toString()).append("' ")
                            .append("style='fill:none;stroke:#211c1d;stroke-width:1.1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-opacity:1;stroke-dasharray:none'")
                            .append(" />");

                    svg.append("</g>");

                    SVGArea currentArea = new SVGArea(x, newY, length, newHeight, transform, 0);
                    RenderingEvent<OWLNamedIndividual> event =
                            new RenderingEvent<OWLNamedIndividual>(individual, svg.toString(), currentArea, this);
                    nexus.renderingEventOccurred(event);

                    // add band to renderedBands set
                    renderedBands.put(band, currentArea);
                }
                else {
                    getLog().error("Unable to render association '" + individual + "' - " +
                                           "no location for band '" + band.getBandName() + "'");
                }
            }
            else {
                // we've already rendered the required association line, so we don't need to do it again
                // but we do need to log the rendering event for this association individual
                getLog().trace("Already rendered an association line to band '" + band.getBandName() + "', " +
                                       "logging secondary event for association '" + individual + "'");
                SVGArea area = renderedBands.get(band);
                nexus.renderingEventOccurred(new RenderingEvent<OWLNamedIndividual>(individual, "", area, this));
            }
        }
        else {
            getLog().error("Cannot render association '" + individual + "' - " +
                                   "unable to identify the band where this association is located");
        }
    }

    public BandInformation getBandInformation(OWLIndividual individual, OWLOntology ontology) {
        String bandName = null;
        OWLDataFactory dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

        //get all the is_about individuals of this trait-assocation
        OWLObjectProperty is_about = dataFactory.getOWLObjectProperty(IRI.create(OntologyConstants.IS_ABOUT_IRI));
        Set<OWLIndividual> related = individual.getObjectPropertyValues(is_about, ontology);
        for (OWLIndividual snp : related) {
            //find the individual that is of type SNP
            for (OWLClassExpression allType : snp.getTypes(ontology)) {
                OWLClass typeClass = allType.asOWLClass();
                if (typeClass.getIRI().equals(IRI.create(OntologyConstants.SNP_CLASS_IRI))) {
                    //get the snp cytogenetic band
                    OWLObjectProperty has_band =
                            dataFactory.getOWLObjectProperty(IRI.create(OntologyConstants.LOCATED_IN_PROPERTY_IRI));

                    Set<OWLIndividual> bands = snp.getObjectPropertyValues(has_band, ontology);
                    if (bands.size() == 1) {
                        OWLIndividual band = bands.iterator().next();

                        //get the band's name
                        OWLDataProperty has_name =
                                dataFactory.getOWLDataProperty(IRI.create(OntologyConstants.HAS_NAME_PROPERTY_IRI));
                        Set<OWLLiteral> bandNames = band.getDataPropertyValues(has_name, ontology);
                        if (bandNames.size() == 1) {
                            bandName = bandNames.iterator().next().getLiteral();
                        }
                        else {
                            throw new RuntimeException(
                                    "Band OWLIndividual '" + band + "' has more than one band name");
                        }
                    }
                    else {
                        if (bands.size() > 1) {
                            getLog().error("SNP '" + snp + "' is located in more than one band - this data is invalid");
                        }
                        else {
                            getLog().error(
                                    "Unable to identify band for SNP '" + snp + "' - this data may not be available");
                        }
                    }
                }
            }
        }
        if (bandName == null) {
            getLog().error("Association '" + individual + "' could not be located on the diagram " +
                                   "(either due to a missing SNP, an unrecognised band name, " +
                                   "more than one genome location or something else).  Source data is invalid.");
            return null;
        }
        else {
            return new BandInformation(bandName);
        }
    }

    public String chromosomeTransform(String chromosome) {
        int position;
        if (chromosome.equals("X")) {
            position = 22;
        }
        else if (chromosome.equals("Y")) {
            position = 23;
        }
        else {
            position = Integer.parseInt(chromosome) - 1;
        }
        int height = SVGCanvas.canvasHeight;
        int width = SVGCanvas.canvasWidth;

        double chromWidth = (double) width / 12;
        double xCoordinate;
        double yCoordinate = 0;

        if (position < 12) {
            xCoordinate = position * chromWidth;
        }
        else {
            xCoordinate = (position - 12) * chromWidth;
            yCoordinate = (double) height / 2;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("translate(");
        builder.append(Double.toString(xCoordinate));
        builder.append(",");
        builder.append(Double.toString(yCoordinate));
        builder.append(")");

        return builder.toString();
    }

    private Set<OWLNamedIndividual> getAllTraitsForAssociation(OWLReasoner reasoner, OWLNamedIndividual association) {
        OWLDataFactory dataFactory = reasoner.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        OWLObjectProperty is_about = dataFactory.getOWLObjectProperty(IRI.create(OntologyConstants.IS_ABOUT_IRI));
        return reasoner.getObjectPropertyValues(association, is_about).getFlattened();
    }

    private void sortBandsWithData(OWLReasoner reasoner) {
        OWLOntology ontology = reasoner.getRootOntology();
        OWLOntologyManager manager = reasoner.getRootOntology().getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();

        Set<BandInformation> bandSet = new HashSet<BandInformation>();
        Map<BandInformation, BandInformation> bandMap = new HashMap<BandInformation, BandInformation>();

        // use the reasoner to get all individuals of type "cytogenic region"
        OWLClass bandCls = factory.getOWLClass(IRI.create(OntologyConstants.CYTOGENIC_REGION_CLASS_IRI));
        getLog().trace("Retrieving all cytogenetic bands to sort into rendering order...");
        Set<OWLNamedIndividual> bands = reasoner.getInstances(bandCls, false).getFlattened();
        getLog().trace("Got " + bands.size() + " bands, starting sorting...");

        for (OWLNamedIndividual band : bands) {
            // get the band name
            OWLDataProperty has_name = factory.getOWLDataProperty(IRI.create(OntologyConstants.HAS_NAME_PROPERTY_IRI));

            if (band.getDataPropertyValues(has_name, ontology).size() != 0) {
                Set<OWLLiteral> bandNames = reasoner.getDataPropertyValues(band, has_name);
                if (bandNames.size() == 0) {
                    getLog().warn("No band name data property value for band individual '" + band + "'");
                }
                else {
                    if (bandNames.size() > 1) {
                        getLog().warn("There are " + bandNames.size() + " band name data property values " +
                                              "for band individual '" + band + "', only using the first observed name");
                    }
                    String bandName = bandNames.iterator().next().getLiteral();

                    BandInformation bandInfo = new BandInformation(bandName);
                    bandSet.add(bandInfo);
                }
            }
        }

        // now we've added all band info, sort the set of unique bands
        List<BandInformation> bandList = new ArrayList<BandInformation>();
        bandList.addAll(bandSet);
        Collections.sort(bandList);

        for (int i = 1; i < bandList.size(); i++) {
            BandInformation band = bandList.get(i);
            BandInformation previousBand = bandList.get(i - 1);
            bandMap.put(band, previousBand);
        }

        getLog().trace("Mapped " + bandMap.keySet().size() + " bands to their 'previous' band");
        previousBandMapByReasoner.put(reasoner, bandMap);
    }
}