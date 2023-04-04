(function () {
    angular
        .module("module-index")
        .controller("cardPreviewController", cardPreviewController)
        // .directive("wcCardPreview", wcCardPreview)

    /* @ngInject */
    function cardPreviewController($scope) {
        this.$onInit = function() {
            console.log("cardPreviewController: card.id = ", $scope.card.id);
        };
    }

    // /* @ngInject */
    // function wcCardPreview() {
    //     return {
    //         restrict: 'E',
    //         scope: {
    //             "card": "=card",
    //             "user": "=user"
    //         },
    //         controller: "cardPreviewController",
    //         templateUrl: function(element, attrs) {
    //             return attrs.templateUrl || 'js/templates/card-preview.html';
    //         }
    //     }
    // }
})();