capitalystNgApp.directive('ngEnter', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.key === 'Enter' ) {
                scope.$apply(function(){
                    scope.$eval(attrs.ngEnter, {'event': event});
                });
                event.preventDefault();
            }
        });
    };
});

capitalystNgApp.directive('ngEscape', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if(event.key === 'Escape') {
                scope.$apply(function(){
                    scope.$eval(attrs.ngEscape, {'event': event});
                });
                event.preventDefault();
            }
        });
    };
});