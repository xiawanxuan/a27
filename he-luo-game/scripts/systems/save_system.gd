extends Node
class_name SaveSystem

signal save_completed(slot_name: String)
signal load_completed(slot_name: String)
signal save_failed(error: String)
signal load_failed(error: String)

const SAVE_DIR := "user://saves/"
const SAVE_EXT := ".save"
const PROGRESS_SLOT := "progress"
const LUO_SHU_SLOT := "luo_shu_save"
const HE_TU_SLOT := "he_tu_save"

var current_slot: String = ""

func _ready() -> void:
	_ensure_save_dir()

func _ensure_save_dir() -> void:
	var dir := DirAccess.open(SAVE_DIR)
	if dir == null:
		DirAccess.make_dir_absolute(SAVE_DIR)

func save_game(slot_name: String, data: Dictionary) -> bool:
	current_slot = slot_name
	var file_path := _get_save_path(slot_name)
	
	var file := FileAccess.open(file_path, FileAccess.WRITE)
	if file == null:
		var err := FileAccess.get_open_error()
		save_failed.emit("无法保存文件: " + str(err))
		return false
	
	var json_str = JSON.stringify(data)
	file.store_string(json_str)
	file.close()
	
	save_completed.emit(slot_name)
	return true

func load_game(slot_name: String) -> Dictionary:
	var file_path := _get_save_path(slot_name)
	
	if not FileAccess.file_exists(file_path):
		load_failed.emit("存档文件不存在")
		return {}
	
	var file := FileAccess.open(file_path, FileAccess.READ)
	if file == null:
		var err := FileAccess.get_open_error()
		load_failed.emit("无法读取存档: " + str(err))
		return {}
	
	var json_str = file.get_as_text()
	file.close()
	
	var parsed = JSON.parse(json_str)
	if parsed.error != OK:
		load_failed.emit("存档文件格式错误")
		return {}
	
	current_slot = slot_name
	load_completed.emit(slot_name)
	return parsed.data

func has_save_slot(slot_name: String) -> bool:
	var file_path := _get_save_path(slot_name)
	return FileAccess.file_exists(file_path)

func delete_save(slot_name: String) -> bool:
	var file_path := _get_save_path(slot_name)
	if FileAccess.file_exists(file_path):
		var err := DirAccess.remove_absolute(file_path)
		return err == OK
	return false

func list_save_slots() -> Array[String]:
	var slots: Array[String] = []
	var dir := DirAccess.open(SAVE_DIR)
	if dir == null:
		return slots
	
	dir.list_dir_begin()
	var file_name := dir.get_next()
	while file_name != "":
		if not dir.current_is_dir() and file_name.ends_with(SAVE_EXT):
			var slot_name := file_name.trim_suffix(SAVE_EXT)
			slots.append(slot_name)
		file_name = dir.get_next()
	dir.list_dir_end()
	
	return slots

func _get_save_path(slot_name: String) -> String:
	return SAVE_DIR + slot_name + SAVE_EXT

func save_luo_shu_state(data: Dictionary) -> bool:
	data["timestamp"] = Time.get_unix_time_from_system()
	return save_game(LUO_SHU_SLOT, data)

func load_luo_shu_state() -> Dictionary:
	if not has_save_slot(LUO_SHU_SLOT):
		return {}
	var data = load_game(LUO_SHU_SLOT)
	if data.is_empty() or data.get("type", "") != "luo_shu":
		return {}
	return data

func has_luo_shu_save() -> bool:
	return has_save_slot(LUO_SHU_SLOT)

func save_he_tu_state(data: Dictionary) -> bool:
	data["timestamp"] = Time.get_unix_time_from_system()
	return save_game(HE_TU_SLOT, data)

func load_he_tu_state() -> Dictionary:
	if not has_save_slot(HE_TU_SLOT):
		return {}
	var data = load_game(HE_TU_SLOT)
	if data.is_empty() or data.get("type", "") != "he_tu":
		return {}
	return data

func has_he_tu_save() -> bool:
	return has_save_slot(HE_TU_SLOT)

func save_level_progress(level_id: String, completed: bool, score: int = 0) -> bool:
	var data = load_game(PROGRESS_SLOT)
	if data.is_empty():
		data = {"levels": {}}
	elif not data.has("levels"):
		data["levels"] = {}
	
	data["levels"][level_id] = {
		"completed": completed,
		"score": score,
		"timestamp": Time.get_unix_time_from_system()
	}
	return save_game(PROGRESS_SLOT, data)

func get_level_progress(level_id: String) -> Dictionary:
	if not has_save_slot(PROGRESS_SLOT):
		return {}
	var data = load_game(PROGRESS_SLOT)
	if data.is_empty() or not data.has("levels"):
		return {}
	return data["levels"].get(level_id, {})

func is_level_completed(level_id: String) -> bool:
	var progress = get_level_progress(level_id)
	return progress.get("completed", false)

func get_last_played_level() -> String:
	var luo_shu_data = load_luo_shu_state()
	var he_tu_data = load_he_tu_state()
	
	var luo_time = luo_shu_data.get("timestamp", 0)
	var he_time = he_tu_data.get("timestamp", 0)
	
	if luo_time == 0 and he_time == 0:
		return ""
	
	if luo_time > he_time:
		if not luo_shu_data.get("completed", false):
			return "luo_shu"
	elif not he_tu_data.get("completed", false):
		return "he_tu"
	
	if luo_shu_data.get("completed", false) and he_tu_data.get("completed", false):
		return ""
	
	if not luo_shu_data.get("completed", false):
		return "luo_shu"
	if not he_tu_data.get("completed", false):
		return "he_tu"
	
	return ""

func auto_save(data: Dictionary) -> bool:
	var save_type = data.get("type", "")
	match save_type:
		"luo_shu":
			return save_luo_shu_state(data)
		"he_tu":
			return save_he_tu_state(data)
		_:
			return false

func auto_load() -> Dictionary:
	var luo_data = load_luo_shu_state()
	var he_data = load_he_tu_state()
	
	var luo_time = luo_data.get("timestamp", 0)
	var he_time = he_data.get("timestamp", 0)
	
	if luo_time == 0 and he_time == 0:
		return {}
	
	if luo_time > he_time:
		return luo_data
	else:
		return he_data

func has_any_save() -> bool:
	return has_luo_shu_save() or has_he_tu_save()
