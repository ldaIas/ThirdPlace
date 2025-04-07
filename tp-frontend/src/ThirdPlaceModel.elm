module ThirdPlaceModel exposing (Model, Msg(..))

import Browser.Navigation exposing (Key)
import JSPorts.Identity.IdentityHandler as Identity
import JSPorts.WebRTC.WebRTCHandler as WebRTCHandler
import JSPorts.Sporran.SporranHandler as SporranHandler
import Url exposing (Url)

{-
-| The model for the whole of the application. It contains the page key, url, and user DID.
-| userDid is initialized using the Identity module and starts off with Nothing.
-}
type alias Model =
    { pageKey : Key
    , pageUrl : Url
    , userDid : Identity.Model
    , authenticated : Bool
    , webRtcHandler : WebRTCHandler.Model
    , sporranHandler : SporranHandler.Model
    }


type Msg
    = CreateAccount
    | AttemptLogin
    | IdentityMsg Identity.Msg
    | SporranMsg SporranHandler.Msg
    | WebRTCMsg WebRTCHandler.Msg
    | UrlChanged Url
    | NoOp
