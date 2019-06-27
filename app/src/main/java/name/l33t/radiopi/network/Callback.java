package name.l33t.radiopi.network;

public interface Callback {
    interface Volume {
        void updateVolume(int volume);
    }
    interface VolumeImplementation {
        void registerVolumeCallback(Volume callbackClass);
    }

    interface Title {
        void updateTitle(String title);
    }
    interface TitleImplementation {
        void registerTitleCallback(Title callbackClass);
    }

    interface NowPlaying {
        void updateNowPlaying(String streamkey);
    }
    interface NowPlayingImplementation {
        void registerNowPlayingCallback(NowPlaying callbackClass);
    }
}
