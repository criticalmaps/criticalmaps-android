package de.stephanlindauer.criticalmaps;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
public class ApplicationTest {

    @Rule
    public ActivityScenarioRule<Main> mActivityRule = new ActivityScenarioRule<>(Main.class);

    @Test
    public void verifyAppLaunches() {
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()));
    }
}
