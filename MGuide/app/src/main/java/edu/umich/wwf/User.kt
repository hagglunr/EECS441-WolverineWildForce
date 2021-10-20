package edu.umich.wwf
import edu.umich.wwf.UpdateUserLocation

class User {
    var latitude = 0.0
    var longitude = 0.0
    var nearest_node = Node(0.0, 0.0, ArrayList())
    var entrance_node = EntranceNode(0.0, 0.0, ArrayList())
    var destination_node = DestinationNode(0.0, 0.0, ArrayList())

    fun getUserLocation() {
        var (longitude, latitude) = UpdateUserLocation.getLocationFromGPS()
    }

    fun getNearestNode() {

    }
}

