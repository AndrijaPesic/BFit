package rs.elfak.mosis.akitoske.bfit.models;

public class ChallengeModel {

    protected String id;
    protected String ownerId;
    protected CoordsModel coords;

    public ChallengeModel() {
        //default constructor
    }

    public ChallengeModel(String ownerId, CoordsModel coords){
        this.ownerId = ownerId;
        this.coords = coords;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public CoordsModel getCoords() {
        return coords;
    }

    public void setCoords(CoordsModel coords) {
        this.coords = coords;
    }

}
