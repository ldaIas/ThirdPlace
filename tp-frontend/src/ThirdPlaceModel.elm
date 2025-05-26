module ThirdPlaceModel exposing (Model, Msg(..))

import Browser.Navigation exposing (Key)
import JSPorts.Identity.IdentityHandler as Identity
import JSPorts.WebRTC.WebRTCHandler as WebRTCHandler
import JSPorts.Sporran.SporranHandler as SporranHandler
import Url exposing (Url)
import Views.Room.RoomModel as RoomModel
import JSPorts.Geohash.GeohashHandler as GeohashHandler

{-
-| The model for the whole of the application. It contains the page key, url, and user DID.
-| userDid is initialized using the Identity module and starts off with Nothing.
-}
type alias Model =
    { -- general state
      pageKey : Key
    , pageUrl : Url
    , userDid : Identity.Model
    , authenticated : Bool
    -- port models
    , webRtcHandler : WebRTCHandler.Model
    , sporranHandler : SporranHandler.Model
    , geohashHandler : GeohashHandler.Model
    -- view models
    , roomHandler : RoomModel.Model
    }


type Msg
    = CreateAccount
    | AttemptLogin
    -- Port Msgs
    | IdentityMsg Identity.Msg
    | SporranMsg SporranHandler.Msg
    | WebRTCMsg WebRTCHandler.Msg
    | GeohashMsg GeohashHandler.Msg
    -- View Msgs
    | RoomMsg RoomModel.Msg
    | UrlChanged Url
    | NoOp
