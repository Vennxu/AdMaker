package android.support.v4.app;

import java.util.List;

public class SupportV4App {

    public static void activityFragmentsNoteStateNotSaved(FragmentActivity activity) {
//        activity.onStateNotSaved();
    }

    public static List<Fragment> activityFragmentsActive(FragmentActivity activity) {
        return ((FragmentManagerImpl) activity.getSupportFragmentManager()).mActive;
    }

    public static int fragmentIndex(Fragment fragment) {
        return fragment.mIndex;
    }

    public static List<Fragment> fragmentChildFragmentManagerActive(Fragment fragment) {
        return ((FragmentManagerImpl) fragment.getChildFragmentManager()).mActive;
    }
}
