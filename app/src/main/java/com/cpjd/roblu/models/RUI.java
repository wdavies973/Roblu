package com.cpjd.roblu.models;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;

import com.cpjd.roblu.R;

import java.io.Serializable;

import lombok.Data;

/**
 * Stores the UI settings for the app.
 *
 * @since 3.5.6
 * @author Will Davies
 */
@Data
public class RUI implements Serializable {

    /**
     * Changing this versionUID will render this class incompatible with older versions.
     */
    public static final long serialVersionUID = 1L;

    /**
     * If true, this class should be re-sent to the server
     */
    public boolean uploadRequired;

    /**
     * primaryColor - toolbar color, some background colors (like floating action button)
     * accent - categories, text fields, highlight color
     * background - self explanatory
     * text - self explanatory
     * buttons - toolbar buttons, form buttons
     * cardColor - self explanatory
     * teamsRadius - how curvy to make the team cards
     * formRadius - how curvy to make the form ca rds
     * dialogDirection - the animation to be used with any given dialog
     * preset - ui looks already predefined by developer
     */

    private int primaryColor;
    private int accent;
    private int background;
    private int text;
    private int buttons;
    private int cardColor;
    private int teamsRadius;
    private int formRadius;
    private int dialogDirection;
    private int preset;

    public RUI() {
        defaults();
    }

    /**
     * Sets the preset var and loads the correct var values
     * @param preset preset code to set
     */
    public void setPreset(int preset) {
        this.preset = preset;

        if(preset == 0) defaults();
        if(preset == 1) light();
    }

    /**
     * Loads the default preset (the most sexy in my opinion)
     */
    private void defaults() {
        primaryColor = -12627531;

        accent = -13784;
        cardColor = -12303292;
        background = -13619152;
        text = -1;
        buttons = -1;

        teamsRadius = 0;
        formRadius = 25;
        dialogDirection = 0;
        preset = 0;
    }

    /**
     * Loads the light preset
     */
    private void light() {
        primaryColor = -14575885;

        accent = -16738680;
        cardColor = -12303292;
        background = Color.WHITE;
        text = -16777216;
        buttons = -16777216;

        teamsRadius = 0;
        formRadius = 25;
        dialogDirection = 2;
        preset = 1;
    }

    /**
     * Gets the resource for the dialog direction variable
     * @return the animation id
     */
    public int getAnimation() {
        if(dialogDirection == 0) return R.style.dialog_animation;
        if(dialogDirection  == 1) return R.style.dialog_left_right;
        else return R.style.fade;
    }

    /**
     * Generate a color a few shades darker than the specified, used for the status bar color
     * @param color the starting color
     * @param factor the factor to darken the starting color by
     * @return the darkened color
     */
    @ColorInt
    public static int darker(@ColorInt int color, @FloatRange(from = 0.0, to = 1.0) float factor) {
        return Color.argb(Color.alpha(color),
                Math.max((int) (Color.red(color) * factor), 0),
                Math.max((int) (Color.green(color) * factor), 0),
                Math.max((int) (Color.blue(color) * factor), 0)
        );
    }
}
