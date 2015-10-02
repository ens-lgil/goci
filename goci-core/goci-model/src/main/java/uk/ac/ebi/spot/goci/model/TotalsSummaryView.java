package uk.ac.ebi.spot.goci.model;

import javax.persistence.MappedSuperclass;

/**
 * Created by emma on 22/09/2015.
 */
@MappedSuperclass
public class TotalsSummaryView {

    private String year;

    private String curator;

    private Integer curatorTotal;

    private String curationStatus;

    // JPA no-args constructor
    public TotalsSummaryView() {
    }

    public TotalsSummaryView(String year, String curator, Integer curatorTotal, String curationStatus) {
        this.year = year;
        this.curator = curator;
        this.curatorTotal = curatorTotal;
        this.curationStatus = curationStatus;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCurator() {
        return curator;
    }

    public void setCurator(String curator) {
        this.curator = curator;
    }

    public Integer getCuratorTotal() {
        return curatorTotal;
    }

    public void setCuratorTotal(Integer curatorTotal) {
        this.curatorTotal = curatorTotal;
    }

    public String getCurationStatus() {
        return curationStatus;
    }

    public void setCurationStatus(String curationStatus) {
        this.curationStatus = curationStatus;
    }
}