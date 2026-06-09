extends Node
class_name GameManager

signal level_started(level_id: String)
signal level_completed(level_id: String)
signal scene_changed(scene_name: String)

var save_system: SaveSystem = null
var current_level: String = ""
var is_paused: bool = false

func _ready() -> void:
	save_system = SaveSystem.new()
	add_child(save_system)

func start_level(level_id: String) -> void:
	current_level = level_id
	level_started.emit(level_id)

func complete_level(level_id: String) -> void:
	if save_system:
		save_system.save_level_progress(level_id, true)
	level_completed.emit(level_id)

func is_level_unlocked(level_id: String) -> bool:
	if level_id == "luo_shu":
		return true
	if level_id == "he_tu":
		return save_system and save_system.is_level_completed("luo_shu")
	return false

func is_level_completed(level_id: String) -> bool:
	if save_system:
		return save_system.is_level_completed(level_id)
	return false

func goto_scene(scene_path: String) -> void:
	get_tree().change_scene_to_file(scene_path)
	scene_changed.emit(scene_path)

func goto_main_menu() -> void:
	goto_scene("res://scenes/main.tscn")

func goto_luo_shu() -> void:
	start_level("luo_shu")
	goto_scene("res://scenes/luo_shu.tscn")

func goto_he_tu() -> void:
	start_level("he_tu")
	goto_scene("res://scenes/he_tu.tscn")

func save_luo_shu_state(data: Dictionary) -> bool:
	if save_system:
		return save_system.save_luo_shu_state(data)
	return false

func save_he_tu_state(data: Dictionary) -> bool:
	if save_system:
		return save_system.save_he_tu_state(data)
	return false

func load_luo_shu_state() -> Dictionary:
	if save_system:
		return save_system.load_luo_shu_state()
	return {}

func load_he_tu_state() -> Dictionary:
	if save_system:
		return save_system.load_he_tu_state()
	return {}

func has_saved_game() -> bool:
	return save_system and save_system.has_any_save()

func get_last_played_level() -> String:
	if save_system:
		return save_system.get_last_played_level()
	return ""

func get_save_system() -> SaveSystem:
	return save_system

func load_game_state() -> Dictionary:
	if save_system:
		return save_system.auto_load()
	return {}

func save_game_state(data: Dictionary) -> bool:
	if save_system:
		return save_system.auto_save(data)
	return false
