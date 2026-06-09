extends "res://scripts/drag_system/draggable_item.gd"
class_name NumberTile

@onready var label: Label = $Label

var number_value: int = 0

func _ready() -> void:
	super._ready()
	if label:
		label.text = str(number_value)
		label.add_theme_color_override("font_color", item_color)
	item_data = number_value

func set_number(value: int) -> void:
	number_value = value
	item_data = value
	if label:
		label.text = str(value)

func get_number() -> int:
	return number_value

func get_item_type() -> String:
	return "number"

func set_correct_state(is_correct: bool) -> void:
	if is_correct:
		add_theme_stylebox_override("panel", _get_correct_stylebox())
	else:
		add_theme_stylebox_override("panel", _get_normal_stylebox())

func _get_correct_stylebox() -> StyleBoxFlat:
	var stylebox := StyleBoxFlat.new()
	stylebox.bg_color = Color(0.2, 0.7, 0.3, 0.9)
	stylebox.border_color = Color(0.4, 0.9, 0.5, 1.0)
	stylebox.border_width_left = 2
	stylebox.border_width_right = 2
	stylebox.border_width_top = 2
	stylebox.border_width_bottom = 2
	stylebox.corner_radius_top_left = 6
	stylebox.corner_radius_top_right = 6
	stylebox.corner_radius_bottom_right = 6
	stylebox.corner_radius_bottom_left = 6
	return stylebox

func _get_normal_stylebox() -> StyleBoxFlat:
	var stylebox := StyleBoxFlat.new()
	stylebox.bg_color = Color(0.1, 0.2, 0.4, 0.9)
	stylebox.border_color = Color(0.5, 0.6, 0.8, 0.6)
	stylebox.border_width_left = 2
	stylebox.border_width_right = 2
	stylebox.border_width_top = 2
	stylebox.border_width_bottom = 2
	stylebox.corner_radius_top_left = 6
	stylebox.corner_radius_top_right = 6
	stylebox.corner_radius_bottom_right = 6
	stylebox.corner_radius_bottom_left = 6
	return stylebox
