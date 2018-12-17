package rs.elfak.mosis.akitoske.bfit.models;

public class CardioModel extends ChallengeModel{

    public int power = 0;

    public CardioModel() {

    }

    public CardioModel(ChallengeType type, String ownerId, CoordsModel coords){
        super(type, ownerId, coords);
    }


}
