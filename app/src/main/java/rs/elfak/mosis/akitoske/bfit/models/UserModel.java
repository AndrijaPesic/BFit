package rs.elfak.mosis.akitoske.bfit.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import rs.elfak.mosis.akitoske.bfit.Constants;

@IgnoreExtraProperties
public class UserModel {

    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_DISPLAY_NAME = "displayName";
    public static final String KEY_USER_FULL_NAME = "fullName";
    public static final String KEY_USER_PHONE = "phone";
    public static final String KEY_USER_AVATAR_URL = "avatarUrl";
    public static final String KEY_USER_POWER = "power";
    public static final String KEY_USER_POINTS = "points";

    private String id;
    private String email;
    private String displayName;
    private String fullName;
    private String phone;
    private String avatarUrl;

    private CoordsModel coords;
    private Map<String, Boolean> friends = new HashMap<>();
    private Map<String, Boolean> friendRequests = new HashMap<>();
    private Map<String, Boolean> challenges = new HashMap<>();
    private int power = Constants.STARTING_POWER;
    private int points = 0;

    public UserModel() {
        // Default constructor required for calls to DataSnapshot.getValue(UserModel.class)
    }

    public UserModel(String id, String email, String displayName, String fullName, String phone, String imgUrl) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUrl = imgUrl;

        this.coords = new CoordsModel(0, 0);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(KEY_USER_ID, id);
        result.put(KEY_USER_EMAIL, email);
        result.put(KEY_USER_DISPLAY_NAME, displayName);
        result.put(KEY_USER_FULL_NAME, fullName);
        result.put(KEY_USER_PHONE, phone);
        result.put(KEY_USER_AVATAR_URL, avatarUrl);
        result.put(KEY_USER_POWER, power);
        result.put(KEY_USER_POINTS, points);
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }

    public Map<String, Boolean> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(Map<String, Boolean> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public Map<String, Boolean> getChallenges() {
        return challenges;
    }

    public void setChallenges(Map<String, Boolean> challenges) {
        this.challenges = challenges;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public CoordsModel getCoords() {
        return coords;
    }

    public void setCoords(CoordsModel coords) {
        this.coords = coords;
    }

}
