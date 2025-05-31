import {setupSporranPorts} from './JSPorts/Sporran/sporran.js';
import {setupIdentityPorts } from './JSPorts/Identity/libp2p-identity.js'
import {setupRoomPorts} from './JSPorts/Geohash/geohash-room.js'
import {setupRoomPubSubPorts} from './JSPorts/RoomPubsub/room-pubsub.js'

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
setupIdentityPorts(app);
setupRoomPorts(app);
setupRoomPubSubPorts(app);
