jQuery(function($){
	$.timepicker.regional['ru'] = {
			timeOnlyTitle: 'Выберите время',
			timeText: 'Время',
			hourText: 'Часы',
			minuteText: 'Минуты',
			secondText: 'Секунды',
			millisecText: 'миллисекунды',
			currentText: 'Сейчас',
			closeText: 'ОК',
			ampm: false
		};
		$.timepicker.setDefaults($.timepicker.regional['ru']);
});