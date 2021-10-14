package edu.umich.mwassink.mapdisplay

object Building {
    var name: String = "Bob and Betty Beyster Building"
    val rooms = LinkedHashMap<String, Room>()
    val entries = LinkedHashMap<String, Entry>()
    var width: Double = 1093.0
    var height: Double = 628.0
    var image = "res/drawable/ic_action_bbb.png" // TODO find a better way to access resource files

}