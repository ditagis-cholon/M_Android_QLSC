package hcm.ditagis.com.cholon.qlsc.utities;


public class Utils {
    public static final int CHANGE_PASSWORD_OLD_PASSWORD_WRONG = -1;
    public static final int CHANGE_PASSWORD_FAILURE = 0;
    public static final int CHANGE_PASSWORD_SUCCESS = 1;

    private static Utils instance = null;

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();

        }

        return instance;
    }

    private Utils() {

    }
}
