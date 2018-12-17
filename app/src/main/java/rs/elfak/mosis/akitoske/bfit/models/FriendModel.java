package rs.elfak.mosis.akitoske.bfit.models;

public class FriendModel {

    public String userId;
    public String displayName;
    public String fullName;
    public String avatarUrl;

    public FriendModel() {
        // Default constructor required for calls to DataSnapshot.getValue(FriendModel.class)
    }

    public FriendModel(String displayName, String fullName, String avatarUrl) {
        this.displayName = displayName;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

}
