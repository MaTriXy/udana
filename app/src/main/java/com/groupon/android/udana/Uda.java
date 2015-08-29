package com.groupon.android.udana;

import android.app.Activity;
import android.os.Bundle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * Utility class that will allow
 * your activities to preserve their members accross
 * rotations.
 */
public class Uda {

    public static final String BUNDLE_KEY_UDANA = "Udana";
    private static int lastKey = 0;
    private static Map<String, Object> lastFieldMap = new HashMap<>();
    private static UdanaActivityLifecycleCallbacks udanaActivityLifecycleCallbacks;

    public static void preserveMembers(Activity activity) {
        if (udanaActivityLifecycleCallbacks == null) {
            udanaActivityLifecycleCallbacks = new UdanaActivityLifecycleCallbacks();
            activity.getApplication().registerActivityLifecycleCallbacks(udanaActivityLifecycleCallbacks);
            udanaActivityLifecycleCallbacks.onActivityCreated(activity, null);
        }
    }

    public static boolean preserveMembers(Activity activity, Bundle bundle) {
        preserveMembers(activity);
        return isPreserved(bundle);
    }

    /**
     * Save the value of all fields annotated with {@link Udana}.
     *
     * @param activity the activity that we scan.
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
     * Find all fields annotated with {@link Udana}.
     *
     * @param activity the activity that we scan.
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

    public static boolean isPreserved(Bundle bundle) {
        if (bundle == null || !bundle.containsKey(BUNDLE_KEY_UDANA)) {
            return false;
        }
        return (bundle.getInt(BUNDLE_KEY_UDANA) == lastKey);
    }

    private static class UdanaActivityLifecycleCallbacks extends DefaultActivityLifecycleCallbacks {
        @Override public void onActivityCreated(Activity activity, Bundle bundle) {
            if (isPreserved(bundle)) {
                restoreFields(activity);
            } else {
                lastFieldMap.clear();
            }
        }

        @Override public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            if (!bundle.containsKey(BUNDLE_KEY_UDANA)) {
                bundle.putInt(BUNDLE_KEY_UDANA, ++lastKey);
                saveFields(activity);
            }
        }
    }
}
