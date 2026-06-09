extends "res://scripts/drag_system/draggable_item.gd"
class_name ElementTile

@onready var label: Label = $Label

var element_name: String = ""

func _ready() -> void:
	super._ready()
	if label and element_name != "":
		label.text = element_name
		label.add_theme_color_override("font_color", Color.BLACK)
	item_data = element_name

func set_element(element: String) -> void:
	element_name = element
	item_data = element
	var color = FiveElements.get_element_color(element)
	item_color = color
	
	if label:
		label.text = element
	
	if has_theme_stylebox_override("panel"):
		var stylebox = get_theme_stylebox("panel")
		if stylebox and stylebox is StyleBoxFlat:
			stylebox.bg_color = color

func get_element() -> String:
	return element_name

func get_item_type() -> String:
	return "element"

func set_correct_state(is_correct: bool) -> void:
	if is_correct:
		modulate = Color(1.2, 1.2, 1.0, 1.0)
	else:
		modulate = Color.WHITE
