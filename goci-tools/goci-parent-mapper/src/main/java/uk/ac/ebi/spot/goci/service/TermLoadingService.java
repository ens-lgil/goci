package uk.ac.ebi.spot.goci.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import uk.ac.ebi.spot.goci.model.Trait;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dani on 15/02/2016.
 */
public class TermLoadingService {


    private static final String DBQUERY =
            "SELECT DISTINCT D.TRAIT, E.TRAIT, E.URI  " +
                    " FROM DISEASE_TRAIT D " +
                    " JOIN STUDY_DISEASE_TRAIT SD ON SD.DISEASE_TRAIT_ID = D.ID " +
                    " JOIN STUDY_EFO_TRAIT SE ON SE.STUDY_ID = SD.STUDY_ID " +
                    " JOIN EFO_TRAIT E ON E.ID = SE.EFO_TRAIT_ID " +
                    " UNION " +
                    " SELECT DISTINCT D.TRAIT, E.TRAIT, E.URI " +
                    " FROM DISEASE_TRAIT D " +
                    " JOIN STUDY_DISEASE_TRAIT SD ON SD.DISEASE_TRAIT_ID = D.ID " +
                    " JOIN ASSOICATION A ON A.STUDY_ID = SD.STUDY_ID " +
                    " JOIN ASSOCIATION_EFO_TRAIT AE ON AE.ASSOCIATION_ID = A.ID " +
                    " JOIN EFO_TRAIT E ON E.ID = AE.EFO_TRAIT_ID";


    private JdbcTemplate jdbcTemplate;


    @Autowired
    public TermLoadingService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Map<String, List<Trait>> getTraits() {
        List<Trait> allTraits = retrieveData();

        Map<String, List<Trait>> traitsByURI = new HashMap<String, List<Trait>>();

        for(Trait trait : allTraits){
            if(traitsByURI.get(trait.getUri()) != null){
                traitsByURI.get(trait.getUri()).add(trait);
            }
            else {
                List<Trait> t = new ArrayList<Trait>();
                t.add(trait);
                traitsByURI.put(trait.getUri(), t);
            }
        }

      return traitsByURI;

    }


    public List<Trait> retrieveData(){
        return getJdbcTemplate().query(DBQUERY, new TraitMapper());
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }


    private class TraitMapper implements RowMapper<Trait> {
        public Trait mapRow(ResultSet rs, int i) throws SQLException {
            String trait = rs.getString(1).trim();
            String efoTerm = rs.getString(2).trim();
            String uri = rs.getString(3).trim();

            return new Trait(trait, efoTerm, uri);
        }
    }


}