package com.sdimosik.smo;

public class Utils {
    public enum State {
        START(0),
        BUFFER(1),
        APPLIANCE(2),
        DONE(3),
        FAIL(4);

        public final int id;

        private State(int id) {
            this.id = id;
        }
    }
}
