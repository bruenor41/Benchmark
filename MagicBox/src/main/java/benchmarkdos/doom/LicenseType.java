package benchmarkdos.doom;

public enum LicenseType {
    free(1),
    tenses(2),
    full(3),
    business(4);

    private int value;

    private LicenseType(int value) {
        this.value = value;
    }

    public int index() {
        return value;
    }

    public static LicenseType get(int value) {
        switch (value) {
            case 1: {
                return free;
            }
            case 2: {
                return tenses;
            }
            case 3: {
                return full;
            }
            case 4: {
                return business;
            }
        }

        return null;
    }

    public String getLicenseName() {
        switch (value) {
            case 1:{
                return "FREE";
            }
            case 2:{
                return "TENSES";
            }
            case 3:{
                return "FULL";
            }
            case 4:{
                return "BUSINESS";
            }
        }

        return null;
    }

    public static LicenseType getCurrentLicense() {
        return LicenseType.free;
    }
}