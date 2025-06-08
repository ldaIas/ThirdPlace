module ThirdPlaceModel exposing (Model, Msg(..))

import Browser.Navigation exposing (Key)
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
    , authenticated : Bool
    -- port models
    , sporranHandler : SporranHandler.Model
    , geohashHandler : GeohashHandler.Model
    -- view models
    , roomHandler : RoomModel.Model
    }


type Msg =
    -- Port Msgs
    SporranMsg SporranHandler.Msg
    | GeohashMsg GeohashHandler.Msg
    -- View Msgs
    | RoomMsg RoomModel.Msg
    | UrlChanged Url
    | NoOp
