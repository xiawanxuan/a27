extends Control

@onready var title_label: Label = $MainPanel/VBox/TitleLabel
@onready var subtitle_label: Label = $MainPanel/VBox/SubtitleLabel
@onready var luo_shu_button: Button = $MainPanel/VBox/ButtonContainer/LuoShuButton
@onready var he_tu_button: Button = $MainPanel/VBox/ButtonContainer/HeTuButton
@onready var continue_button: Button = $MainPanel/VBox/ButtonContainer/ContinueButton
@onready var quit_button: Button = $MainPanel/VBox/ButtonContainer/QuitButton
@onready var status_label: Label = $MainPanel/VBox/StatusLabel

func _ready() -> void:
	_connect_signals()
	_update_button_states()

func _connect_signals() -> void:
	luo_shu_button.pressed.connect(_on_luo_shu_pressed)
	he_tu_button.pressed.connect(_on_he_tu_pressed)
	continue_button.pressed.connect(_on_continue_pressed)
	quit_button.pressed.connect(_on_quit_pressed)

func _update_button_states() -> void:
	var has_save := false
	var last_scene := ""
	
	if GameManager and GameManager.save_system:
		has_save = GameManager.has_saved_game()
		var data = GameManager.load_game_state()
		if not data.is_empty():
			last_scene = data.get("type", "")
	
	continue_button.disabled = not has_save
	
	if GameManager:
		var luo_completed = GameManager.is_level_unlocked("luo_shu")
		var he_completed = GameManager.is_level_unlocked("he_tu")
		
		if he_completed:
			he_tu_button.disabled = false
			status_label.text = "洛书: ✓   河图: 已解锁"
		else:
			he_tu_button.disabled = true
			status_label.text = "洛书: 待完成   河图: 未解锁"

func _on_luo_shu_pressed() -> void:
	if GameManager:
		GameManager.goto_luo_shu()

func _on_he_tu_pressed() -> void:
	if GameManager and GameManager.is_level_unlocked("he_tu"):
		GameManager.goto_he_tu()
	else:
		_show_message("请先完成洛书九宫格！")

func _on_continue_pressed() -> void:
	if not GameManager or not GameManager.has_saved_game():
		return
	
	var data = GameManager.load_game_state()
	var save_type = data.get("type", "")
	
	match save_type:
		"luo_shu":
			if GameManager:
				GameManager.goto_luo_shu()
		"he_tu":
			if GameManager:
				GameManager.goto_he_tu()
		_:
			_show_message("没有可继续的游戏")

func _on_quit_pressed() -> void:
	get_tree().quit()

func _show_message(message: String) -> void:
	var dialog := AcceptDialog.new()
	dialog.title = "提示"
	dialog.dialog_text = message
	dialog.ok_button_text = "确定"
	add_child(dialog)
	dialog.popup_centered()
