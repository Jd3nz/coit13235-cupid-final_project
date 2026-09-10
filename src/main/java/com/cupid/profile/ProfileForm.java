package com.cupid.profile;

import com.cupid.matching.model.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Web form data for FR_Profile. Bean-validation rules provide a first line of
 * defence against invalid browser input; ProfileService repeats the essential
 * checks before a profile is persisted.
 */
public class ProfileForm {

    @NotBlank(message = "Display name is required.")
    @Size(max = 100, message = "Display name must be 100 characters or fewer.")
    private String displayName;

    @NotNull(message = "Age is required.")
    @Min(value = 18, message = "Cupid profiles are available from age 18.")
    @Max(value = 120, message = "Enter a realistic age of 120 or below.")
    private Integer age;

    @Size(max = 500, message = "Bio must be 500 characters or fewer.")
    private String bio;

    public ProfileForm() {
        // Required for Spring MVC form binding.
    }

    public ProfileForm(String displayName, Integer age, String bio) {
        this.displayName = displayName;
        this.age = age;
        this.bio = bio;
    }

    public static ProfileForm from(User user) {
        return new ProfileForm(
                user.getDisplayName(),
                user.getAge(),
                user.getBio()
        );
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
