import {setupSporranPorts} from './JSPorts/Sporran/sporran';
import {setupRoomPorts} from './JSPorts/Geohash/geohash-room'
import {setupRoomPubSubPorts} from './JSPorts/RoomPubsub/room-pubsub'

declare namespace Elm {
  interface App<Flags, Ports> {
    init(options: { node: HTMLElement; flags?: Flags }): Ports;
  }

  const ThirdPlaceApp: {
    init(options: { node: HTMLElement }): any;
  };
}

const app = Elm.ThirdPlaceApp.init({node: document.getElementById('app')!});
setupSporranPorts(app);
setupRoomPorts(app);
setupRoomPubSubPorts(app);
