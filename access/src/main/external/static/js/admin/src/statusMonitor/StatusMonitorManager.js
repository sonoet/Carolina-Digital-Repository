define('StatusMonitorManager', [ 'jquery', 'jquery-ui', 'underscore', 'DepositMonitor', 'IndexingMonitor', 'EnhancementMonitor'],
		function($, ui, _, DepositMonitor, IndexingMonitor, EnhancementMonitor) {
			
	function StatusMonitorManager(element, options) {
		this.element = element;
		this.options = options;
		this.tabList = $("<ul/>").attr("id", "status_monitor_tabs").appendTo(this.element);
		this.monitors = [];
		this.addMonitors();
		var self = this;
		this.element.tabs({
			beforeActivate : function(event, ui) {
				// Deactivate the currently active monitor
				self.deactivate();
				// Activate the selected monitor
				var index = ui.newTab.index();
				self.activate(index);
			},
			activate : function() {
				self.monitors[self.activeMonitorIndex].positionDetailsView();
			}
		});
		this.activeMonitorIndex = 0;
	};
	
	StatusMonitorManager.prototype.deactivate = function(index) {
		index = arguments.length > 0? index : this.activeMonitorIndex;
		this.monitors[index].deactivate();
	};
	
	StatusMonitorManager.prototype.activate = function(index) {
		index = arguments.length > 0? index : this.activeMonitorIndex;
		this.activeMonitorIndex = index;
		this.monitors[index].activate();
	};
	
	StatusMonitorManager.prototype.addMonitors = function() {
		this.addMonitor(new DepositMonitor(this.options));
		this.addMonitor(new IndexingMonitor());
		this.addMonitor(new EnhancementMonitor());
	};
	
	StatusMonitorManager.prototype.addMonitor = function(monitor) {
		this.monitors.push(monitor);
		monitor.init();
		monitor.element.appendTo(this.element);
		this.tabList.append("<li><a href='" + document.URL + "#" + monitor.monitorId + "'>" + monitor.options.name + "</a></li>");
	};
	
	return StatusMonitorManager;
});