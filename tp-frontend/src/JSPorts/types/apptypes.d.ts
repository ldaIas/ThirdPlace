export interface App {
    ports: {

        /* sporran ports */
        detectSporran: {
            subscribe: (callback: () => Promise<void>) => void;
        };
        onSporranDetected: {
            send: (detected: boolean) => void;
        };
        requestLogin: {
            subscribe: (callback: () => Promise<void>) => void;
        };
        onLoginAttempted: {
            send: (data: null) => void;
        };
        onLoginSuccess: {
            send: (message: string) => void;
        };


        /* room-pubsub ports */
        joinRoom: {
            subscribe: (callback: (roomId: string) => Promise<void>) => void;
        };
        sendMessage: {
            subscribe: (callback: (message: any) => Promise<void>) => void;
        };
        receiveConversation: {
            send: (message: any) => void;
        };
        receiveMessage: {
            send: (message: any) => void;
        };

        
        /* geohash ports */
        requestRoomId: {
            subscribe: (callback: () => void) => void;
        };
        receiveRoomId: {
            send: (geohash: string) => void;
        };

    };
}