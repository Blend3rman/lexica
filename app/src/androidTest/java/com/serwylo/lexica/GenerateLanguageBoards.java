package com.serwylo.lexica;


import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.serwylo.lexica.lang.Language;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

/**
 * The current intention of this test is to be run manually before a release, rather than via
 * CI on every commit. Once it tests something more meaningful than starting a game and ending it
 * (e.g. simulating users dragging their fingers on a real board, selecting real words) then it
 * should probably be promoted to CI.
 */
@LargeTest
@RunWith(Parameterized.class)
public class GenerateLanguageBoards {

    private static final String TAG = "GenerateLanguageBoards";

    private static final int NUM_BOARDS_TO_GENERATE = 3;


    /**
     * Each language should have a static initializer block which contains a Scrabble-like score for
     * each letter. The compiler cannot enforce that we have
     */
    private final Language language;

    public GenerateLanguageBoards(Language language) {
        super();
        this.language = language;
    }

    @Parameterized.Parameters
    public static List<Language[]> getAllLanguages() {
        List<Language[]> langs = new ArrayList<>(Language.getAllLanguages().size());
        for (Language lang : Language.getAllLanguages().values()) {
            langs.add(new Language[] { lang });
        }
        return langs;
    }

    @Test
    public void generateBoards() {
        generateLanguageBoards(this.language);
    }

    @Rule
    public ActivityTestRule<Lexica> mActivityTestRule = new ActivityTestRule<>(Lexica.class);

    private void generateLanguageBoards(Language language) {
        Log.d(TAG, "Running test for " + language.getName() + " board");
        fromHomeNavigateToPreferences();
        fromPreferencesSelectLanguage(language);
        fromPreferencesSelectBoardSize();
        fromPreferencesSelectScoreType();
        pressBack();

        for (int i = 0; i < NUM_BOARDS_TO_GENERATE; i ++) {
            fromHomeStartNewGame();
            fromGameRotateBoard();
            fromGameSaveGame();
            fromHomeRestoreGame();
            fromGameRotateBoard();
            fromGameEndGame();
            fromPostGameSelectMissingWords();
            fromMissingWordsViewWord(0);
            fromMissingWordsViewWord(1);
            fromMissingWordsReturnHome();
        }
    }

    private void fromHomeNavigateToPreferences() {
        ViewInteraction preferencesButton = onView(
                allOf(withId(R.id.preferences), withText("Preferences"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        3),
                                0),
                        isDisplayed()));
        preferencesButton.perform(click());
    }

    private void fromPreferencesSelectLanguage(Language language) {
        DataInteraction dictionaryPreference = onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)))
                .atPosition(0);
        dictionaryPreference.perform(click());

        DataInteraction selectLangView = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(getIndexForLanguage(language));
        selectLangView.perform(click());
    }

    private void fromPreferencesSelectBoardSize() {
        DataInteraction boardSizePreference = onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)))
                .atPosition(1);
        boardSizePreference.perform(click());

        DataInteraction selectBoardSizeView = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(0);
        selectBoardSizeView.perform(click());
    }

    private void fromPreferencesSelectScoreType() {
        DataInteraction selectScoreTypePreference = onData(anything())
                .inAdapterView(allOf(withId(android.R.id.list),
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                0)))
                .atPosition(3);
        selectScoreTypePreference.perform(click());

        DataInteraction selectScoreTypeView = onData(anything())
                .inAdapterView(allOf(withClassName(is("com.android.internal.app.AlertController$RecycleListView")),
                        childAtPosition(
                                withClassName(is("android.widget.FrameLayout")),
                                0)))
                .atPosition(1);
        selectScoreTypeView.perform(click());
    }

    private int getIndexForLanguage(Language language) {
        int langIndex = -1;
        String[] languageValues = getTargetContext().getResources().getStringArray(R.array.dict_choices_entryvalues);
        for (int i = 0; i < languageValues.length; i ++) {
            String lang = languageValues[i];
            if (lang.equals(language.getName()) || language.getName().equals("en_US") && lang.equals("US") || language.getName().equals("en_GB") && lang.equals("UK")) {
                langIndex = i;
                break;
            }
        }
        assertTrue(langIndex >= 0);
        return langIndex;
    }

    private void fromHomeStartNewGame() {
        ViewInteraction newGameButton = onView(allOf(withId(R.id.new_game), isDisplayed()));
        newGameButton.perform(click());
    }

    private void fromGameRotateBoard() {
        ViewInteraction rotateMenuItem = onView(
                allOf(withId(R.id.rotate), withContentDescription("Rotate Board"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                0),
                        isDisplayed()));
        rotateMenuItem.perform(click());

    }

    private void fromGameSaveGame() {
        ViewInteraction saveMenuItem = onView(
                allOf(withId(R.id.save_game), withContentDescription("Save Game"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                1),
                        isDisplayed()));
        saveMenuItem.perform(click());
    }

    private void fromHomeRestoreGame() {
        ViewInteraction restoreButton = onView(
                allOf(withId(R.id.restore_game), withText("Restore Game"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        2),
                                1),
                        isDisplayed()));
        restoreButton.perform(click());
    }

    private void fromGameEndGame() {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction endGameMenuItem = onView(
                allOf(withId(R.id.title), withText("End Game"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        endGameMenuItem.perform(click());
    }

    private void fromPostGameSelectMissingWords() {

        ViewInteraction missingWordsTab = onView(
                allOf(childAtPosition(
                        allOf(withId(android.R.id.tabs),
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        0)),
                        1),
                        isDisplayed()));
        missingWordsTab.perform(click());
    }

    private void fromMissingWordsViewWord(int index) {
        ViewInteraction wordView = onView(
                allOf(withText("View"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        index),
                                2)));
        wordView.perform(scrollTo(), click());
    }

    private void fromMissingWordsReturnHome() {
        ViewInteraction homeButton = onView(
                allOf(withId(R.id.missed_close_score), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.missed_words),
                                        childAtPosition(
                                                withClassName(is("android.widget.FrameLayout")),
                                                1)),
                                2),
                        isDisplayed()));
        homeButton.perform(click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
