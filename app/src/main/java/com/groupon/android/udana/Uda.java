package com.groupon.android.udana;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Utility class that allows
 * activities to preserve their members across
 * rotations.
 */
public class Uda {

    public static final String BUNDLE_KEY_UDANA = "Udana";
    private static int activityMetaScopeCounter = 0;
    private static Map<String, Object> lastFieldMap = new HashMap<>();
    private static UdanaActivityLifecycleCallbacks udanaActivityLifecycleCallbacks;

    /**
     * Preserves the activity members across rotations.
     * When an activity rotates and creates a new instance
     * of the same class, all {@link Udana} annotated fields
     * will be passed to the next instance.
     * <p/>
     * There are 2 different ways to use this method :
     * <ul>
     *     <li>inside a custom application class's {@code onCreate} method.</li>
     *     <li>OR on a per activity basis. Inside the {@code onCreate} methods.</li>
     * </ul>
     * @param application the application inside which all activities' fields that
     * are annotated with {@link Udana} will be preserved across rotation.
     * @see #preserveMembers(Activity)
     */
    public static void preserveMembers(Application application) {
        if (udanaActivityLifecycleCallbacks == null) {
            udanaActivityLifecycleCallbacks = new UdanaActivityLifecycleCallbacks();
            application.registerActivityLifecycleCallbacks(udanaActivityLifecycleCallbacks);
        }
    }

    /**
     * Preserves the activity members across rotations.
     * When an activity rotates and creates a new instance
     * of the same class, all {@link Udana} annotated fields
     * will be passed to the next instance.
     * <p/>
     * There are 2 different ways to use this method :
     * <ul>
     *     <li>inside a custom application class's {@code onCreate} method.</li>
     *     <li>OR on a per activity basis. Inside the {@code onCreate} methods.</li>
     * </ul>
     * @param activity the activity whose fields that
     * are annotated with {@link Udana} will be preserved across rotation.
     * @see #preserveMembers(Application)
     */
    public static void preserveMembers(Activity activity) {
        if (udanaActivityLifecycleCallbacks == null) {
            preserveMembers(activity.getApplication());
            udanaActivityLifecycleCallbacks.onActivityCreated(activity, null);
        }
    }

    /**
     * Preserves the activity members across rotations.
     * When an activity rotates and creates a new instance
     * of the same class, all {@link Udana} annotated fields
     * will be passed to the next instance.
     * @param activity the activity whose fields that
     * are annotated with {@link Udana} will be preserved across rotation.
     * @return Returns true if the instance of the
     * activity class {@code activity} has received values from a previous
     * instance. False otherwise.
     * @see #preserveMembers(Application)
     * @see #preserveMembers(Activity)
     */
    public static boolean preserveMembers(Activity activity, Bundle bundle) {
        preserveMembers(activity);
        return isPreserved(bundle);
    }

    /**
     * @param bundle the bundle of the new activity after rotation.
     * @return Returns true if the instance of the
     * activity class {@code activity} has received values from a previous
     * instance. False otherwise.
     * @see #preserveMembers(Activity, Bundle)
     */
    public static boolean isPreserved(Bundle bundle) {
        if (bundle == null || !hasUdanaKey(bundle)) {
            return false;
        }
        return hasLastUdanaKey(bundle);
    }

    /**
     * Save the value of all fields annotated with {@link Udana}.
     * @param activity the activity instance right before rotation.
     */
    private static void saveFields(Activity activity) {
        Field[] udanaFields = findUdanaFields(activity);
        lastFieldMap.clear();
        for (Field udanaField : udanaFields) {
            try {
                lastFieldMap.put(udanaField.getName(), udanaField.get(activity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Should not happen.", e);
            }
        }
    }

    /**
     * Restores the last saved field of a previous instance before rotation
     * into the new instance after the rotation {@activtity}.
     * @param activity the new instance after rotation.
     */
    private static void restoreFields(Activity activity) {
        Field[] udanaFields = findUdanaFields(activity);
        for (Field udanaField : udanaFields) {
            final Object fieldValue = lastFieldMap.get(udanaField.getName());
            if (fieldValue == null) {
                throw new RuntimeException(format("The value of the udanaField %s in %s was not found.", udanaField.getName(), activity.getClass().getName()));
            }
            try {
                udanaField.set(activity, fieldValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(format("The udanaField %s is not accesssible in %s", udanaField.getName(), activity.getClass().getName()), e);
            }
        }
    }

    /**
     * Find all fields annotated with {@link Udana}.
     * @param activity the activity whose fields are scanned.
     * @return the fields annotated with {@link Udana} in {@code activity}.
     */
    private static Field[] findUdanaFields(Activity activity) {
        List<Field> udanaFieldList = new ArrayList<>();

        Field[] fields = activity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Udana.class) != null) {
                field.setAccessible(true);
                udanaFieldList.add(field);
            }
        }

        return udanaFieldList.toArray(new Field[udanaFieldList.size()]);
    }

    /**
     * @param bundle
     * @return true if the bundle contains the Udana key. False otherwise.
     */
    private static boolean hasUdanaKey(Bundle bundle) {
        return bundle.containsKey(BUNDLE_KEY_UDANA);
    }

    /**
     * If a bundle contains the last Udana key, it means that the activity
     * that is associated to it (that receives this bundle during its {@code onCreate}
     * method) is a new instance of the same class after a rotation.
     * @param bundle
     * @return true if the bundle contains the last Udana key. False otherwise.
     */
    private static boolean hasLastUdanaKey(Bundle bundle) {
        return bundle.getInt(BUNDLE_KEY_UDANA) == activityMetaScopeCounter;
    }

    /**
     * Increment the Udana key and Adds it to a bundle.
     * If we find the same key again, then activity associated with it
     * is a new instance of the same class after a rotation. If the key found
     * is different then it's just an activity that has been seen by Udana.
     * @param bundle
     * @return true if the bundle contains the last Udana key. False otherwise.
     */
    private static void putUdanaKey(Bundle bundle) {
        bundle.putInt(BUNDLE_KEY_UDANA, ++activityMetaScopeCounter);
    }

    /**
     * Lifecycle callback that will monitor all activities in the app.
     */
    private static class UdanaActivityLifecycleCallbacks extends DefaultActivityLifecycleCallbacks {
        @Override public void onActivityCreated(Activity activity, Bundle bundle) {
            if (isPreserved(bundle)) {
                restoreFields(activity);
            } else {
                lastFieldMap.clear();
            }
        }

        @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            if (!hasUdanaKey(bundle)) {
                putUdanaKey(bundle);
                saveFields(activity);
            }
        }
    }
}
