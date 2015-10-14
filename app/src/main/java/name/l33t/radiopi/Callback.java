package name.l33t.radiopi;


public class Callback {
    /*Setup for volume callback*/
    interface Volume {
        void setVolume(Integer vol);
    }

    interface VolumeImplementation {
        void registerVolumeCallback(Volume callbackClass);
    }

    /*Setup for message callback*/
    interface Message {
        void displayToast(String message);
    }

    interface MessageImplementation {
        void registerMessageCallback(Message callbackClass);
    }
}


