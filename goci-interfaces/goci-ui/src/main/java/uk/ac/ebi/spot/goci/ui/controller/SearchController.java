package uk.ac.ebi.spot.goci.ui.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.spot.goci.model.SearchResult;
import uk.ac.ebi.spot.goci.repository.*;

/**
 * Created by dwelter on 12/01/15.
 */

@Controller
public class SearchController {

    private AssociationRepository associationRepository;
    private DiseaseTraitRepository diseaseTraitRepository;
    private EfoTraitRepository efoTraitRepository;
    private SingleNucleotidePolymorphismRepository singleNucleotidePolymorphismRepository;
    private StudyRepository studyRepository;


    @Autowired
    public SearchController(AssociationRepository associationRepository, DiseaseTraitRepository diseaseTraitRepository, EfoTraitRepository efoTraitRepository, SingleNucleotidePolymorphismRepository singleNucleotidePolymorphismRepository, StudyRepository studyRepository){

        this.associationRepository = associationRepository;
        this.diseaseTraitRepository = diseaseTraitRepository;
        this.efoTraitRepository = efoTraitRepository;
        this.singleNucleotidePolymorphismRepository = singleNucleotidePolymorphismRepository;
        this.studyRepository = studyRepository;
    }

    @RequestMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
    String search(Model model, @RequestParam(required=false) String query){
//        model.addAttribute("search", studyRepository.findAll());

        System.out.println(query);

        SearchResult result = new SearchResult();
        result.setQuery(query);

        model.addAttribute("result", result);

        return "search";


    }

//    @RequestMapping(value = "/search", produces = MediaType.TEXT_HTML_VALUE)
//    String search(Model model, @RequestParam(required=false) String query, @RequestParam String facet){
////        model.addAttribute("search", studyRepository.findAll());
//
//        System.out.println(query);
//
//        SearchResult result = new SearchResult();
//        result.setQuery(query);
//        result.setFacet(facet);
//
//        model.addAttribute("result", result);
//
//        return "search";
//
//
//    }
}
