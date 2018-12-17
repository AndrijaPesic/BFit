package rs.elfak.mosis.akitoske.bfit.models;

import rs.elfak.mosis.akitoske.bfit.R;

public enum ChallengeType {

    // The string names are for views in the app
    CARDIO("Cardio", R.drawable.ic_cardio, 3, 50),
    STRENGTH("Strength", R.drawable.ic_strength, 2, 100);

    private String name;
    private int iconResId;
    private int maxLevel;
    private int baseCost;

    ChallengeType(String name, int iconResourceId, int maxLevel, int baseCost){
        this.name = name;
        this.iconResId = iconResourceId;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
    }

    public String getName() {
        return this.name;
    }

    public int getIconResId() {
        return this.iconResId;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getBaseCost() {
        return this.baseCost;
    }

}
