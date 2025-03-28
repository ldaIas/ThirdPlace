module ThirdPlaceModel exposing (Model, Msg(..))

import Browser.Navigation exposing (Key)
import JSPorts.Identity.IdentityHandler as Identity
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
    }


type Msg
    = CreateAccount
    | AttemptLogin
    | IdentityMsg Identity.Msg
    | UrlChanged Url
    | NoOp
