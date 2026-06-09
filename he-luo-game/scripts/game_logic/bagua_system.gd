extends RefCounted
class_name BaguaSystem

const QIAN = "乾"
const KUN = "坤"
const ZHEN = "震"
const XUN = "巽"
const KAN = "坎"
const LI = "离"
const GEN = "艮"
const DUI = "兑"

const EIGHT_TRIGRAMS := [QIAN, KUN, ZHEN, XUN, KAN, LI, GEN, DUI]

const TRIGRAM_SYMBOLS := {
	QIAN: "☰",
	KUN: "☷",
	ZHEN: "☳",
	XUN: "☴",
	KAN: "☵",
	LI: "☲",
	GEN: "☶",
	DUI: "☱"
}

const TRIGRAM_LINES := {
	QIAN: [1, 1, 1],
	KUN: [0, 0, 0],
	ZHEN: [1, 0, 0],
	XUN: [0, 1, 1],
	KAN: [0, 1, 0],
	LI: [1, 0, 1],
	GEN: [0, 0, 1],
	DUI: [1, 1, 0]
}

const TRIGRAM_ELEMENTS := {
	QIAN: "金",
	KUN: "土",
	ZHEN: "木",
	XUN: "木",
	KAN: "水",
	LI: "火",
	GEN: "土",
	DUI: "金"
}

const TRIGRAM_EXPLANATIONS := {
	QIAN: {
		"name": "乾为天",
		"nature": "天",
		"element": "金",
		"direction": "西北（后天）/南（先天）",
		"meaning": "刚健、自强不息、创造、领导",
		"description": "乾卦三爻皆阳，象征天的刚健不息。代表创造力、领导力和积极进取的精神。在家庭中代表父亲，在身体中代表头部。",
		"virtue": "元亨利贞"
	},
	KUN: {
		"name": "坤为地",
		"nature": "地",
		"element": "土",
		"direction": "西南（后天）/北（先天）",
		"meaning": "柔顺、厚德载物、包容、滋养",
		"description": "坤卦三爻皆阴，象征大地的包容与滋养。代表顺从、承载和默默奉献的精神。在家庭中代表母亲，在身体中代表腹部。",
		"virtue": "厚德载物"
	},
	ZHEN: {
		"name": "震为雷",
		"nature": "雷",
		"element": "木",
		"direction": "东（后天）/东北（先天）",
		"meaning": "震动、奋发、行动、长子",
		"description": "震卦一阳在下，象征春雷初响、万物复苏。代表行动、振奋和突破。在家庭中代表长子，在身体中代表足。",
		"virtue": "恐惧修省"
	},
	XUN: {
		"name": "巽为风",
		"nature": "风",
		"element": "木",
		"direction": "东南（后天）/西南（先天）",
		"meaning": "谦逊、渗透、灵活、长女",
		"description": "巽卦一阴在下，象征风的无处不在。代表谦逊、灵活和深入。在家庭中代表长女，在身体中代表股（大腿）。",
		"virtue": "申命行事"
	},
	KAN: {
		"name": "坎为水",
		"nature": "水",
		"element": "水",
		"direction": "北（后天）/西（先天）",
		"meaning": "危险、智慧、流动、中男",
		"description": "坎卦阳在中阴，象征水的流动与险陷。代表智慧、适应和坚持。在家庭中代表中男，在身体中代表耳。",
		"virtue": "常德行习教事"
	},
	LI: {
		"name": "离为火",
		"nature": "火",
		"element": "火",
		"direction": "南（后天）/东（先天）",
		"meaning": "光明、依附、文明、中女",
		"description": "离卦阴在中阳，象征火的光明与依附。代表光明、文明和热情。在家庭中代表中女，在身体中代表目。",
		"virtue": "明两作离"
	},
	GEN: {
		"name": "艮为山",
		"nature": "山",
		"element": "土",
		"direction": "东北（后天）/西北（先天）",
		"meaning": "静止、稳重、止欲、少男",
		"description": "艮卦一阳在上，象征山的稳重静止。代表停止、稳重和克制。在家庭中代表少男，在身体中代表手。",
		"virtue": "思不出其位"
	},
	DUI: {
		"name": "兑为泽",
		"nature": "泽",
		"element": "金",
		"direction": "西（后天）/东南（先天）",
		"meaning": "喜悦、交流、少女、沼泽",
		"description": "兑卦一阴在上，象征沼泽的喜悦与交流。代表喜悦、沟通和和谐。在家庭中代表少女，在身体中代表口。",
		"virtue": "朋友讲习"
	}
}

static func get_prenatal_bagua() -> Array:
	return [
		{"position": 1, "trigram": QIAN, "luoshu_number": 9},
		{"position": 2, "trigram": DUI, "luoshu_number": 4},
		{"position": 3, "trigram": LI, "luoshu_number": 3},
		{"position": 4, "trigram": ZHEN, "luoshu_number": 8},
		{"position": 5, "trigram": XUN, "luoshu_number": 2},
		{"position": 6, "trigram": KAN, "luoshu_number": 7},
		{"position": 7, "trigram": GEN, "luoshu_number": 6},
		{"position": 8, "trigram": KUN, "luoshu_number": 1}
	]

static func get_postnatal_bagua() -> Array:
	return [
		{"position": 1, "trigram": KAN, "luoshu_number": 1},
		{"position": 2, "trigram": KUN, "luoshu_number": 2},
		{"position": 3, "trigram": ZHEN, "luoshu_number": 3},
		{"position": 4, "trigram": XUN, "luoshu_number": 4},
		{"position": 5, "trigram": QIAN, "luoshu_number": 6},
		{"position": 6, "trigram": DUI, "luoshu_number": 7},
		{"position": 7, "trigram": GEN, "luoshu_number": 8},
		{"position": 8, "trigram": LI, "luoshu_number": 9}
	]

static func get_prenatal_bagua_from_luoshu(luoshu_grid: Array) -> Array:
	var result := []
	var positions := [
		{"row": 0, "col": 1, "trigram": QIAN},
		{"row": 0, "col": 0, "trigram": DUI},
		{"row": 1, "col": 0, "trigram": LI},
		{"row": 2, "col": 0, "trigram": ZHEN},
		{"row": 2, "col": 1, "trigram": KUN},
		{"row": 2, "col": 2, "trigram": GEN},
		{"row": 1, "col": 2, "trigram": KAN},
		{"row": 0, "col": 2, "trigram": XUN}
	]
	
	for pos in positions:
		var number = luoshu_grid[pos.row][pos.col]
		result.append({
			"trigram": pos.trigram,
			"number": number,
			"row": pos.row,
			"col": pos.col
		})
	
	return result

static func get_postnatal_bagua_from_luoshu(luoshu_grid: Array) -> Array:
	var result := []
	var positions := [
		{"row": 2, "col": 0, "trigram": KAN},
		{"row": 2, "col": 1, "trigram": KUN},
		{"row": 1, "col": 0, "trigram": ZHEN},
		{"row": 0, "col": 0, "trigram": XUN},
		{"row": 0, "col": 1, "trigram": LI},
		{"row": 0, "col": 2, "trigram": QIAN},
		{"row": 1, "col": 2, "trigram": DUI},
		{"row": 2, "col": 2, "trigram": GEN}
	]
	
	for pos in positions:
		var number = luoshu_grid[pos.row][pos.col]
		result.append({
			"trigram": pos.trigram,
			"number": number,
			"row": pos.row,
			"col": pos.col
		})
	
	return result

static func get_trigram_explanation(trigram: String) -> Dictionary:
	if TRIGRAM_EXPLANATIONS.has(trigram):
		return TRIGRAM_EXPLANATIONS[trigram].duplicate(true)
	return {}

static func get_trigram_symbol(trigram: String) -> String:
	if TRIGRAM_SYMBOLS.has(trigram):
		return TRIGRAM_SYMBOLS[trigram]
	return "?"

static func get_trigram_lines(trigram: String) -> Array:
	if TRIGRAM_LINES.has(trigram):
		return TRIGRAM_LINES[trigram].duplicate()
	return []

static func get_trigram_element(trigram: String) -> String:
	if TRIGRAM_ELEMENTS.has(trigram):
		return TRIGRAM_ELEMENTS[trigram]
	return ""

static func get_all_trigrams() -> Array[String]:
	return EIGHT_TRIGRAMS.duplicate()

static func is_valid_trigram(trigram: String) -> bool:
	return EIGHT_TRIGRAMS.has(trigram)

static func get_bagua_by_elements(elements_dict: Dictionary) -> Array:
	var result := []
	for element in elements_dict:
		var trigrams_for_element: Array[String] = []
		for trigram in EIGHT_TRIGRAMS:
			if TRIGRAM_ELEMENTS[trigram] == element:
				trigrams_for_element.append(trigram)
		if not trigrams_for_element.is_empty():
			result.append({
				"element": element,
				"trigrams": trigrams_for_element
			})
	return result
