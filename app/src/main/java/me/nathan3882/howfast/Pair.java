package me.nathan3882.howfast;

public class Pair {

    protected Object key;
    protected Object value;

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public class BooleanBooleanPair extends Pair {

        public BooleanBooleanPair(Boolean key, Boolean value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Boolean getKey() {
            return (Boolean) key;
        }

        @Override
        public Boolean getValue() {
            return (Boolean) value;
        }


    }

    public class KeyObjectPair extends Pair {

        public KeyObjectPair(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return (String) super.key;
        }

        @Override
        public Object getValue() {
            return super.value;
        }
    }


    public class IntegerIntegerPair extends Pair {
        public IntegerIntegerPair(int key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
            return (Integer) super.getKey();
        }

        @Override
        public Integer getValue() {
            return (Integer) super.getValue();
        }
    }
}
