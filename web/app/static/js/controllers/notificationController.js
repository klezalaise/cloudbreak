'use strict';

var log = log4javascript.getLogger("notificationController-logger");

angular.module('uluwatuControllers').controller('notificationController', ['$scope', '$rootScope', '$filter', 'Cluster', 'GlobalStack', 'UluwatuCluster',
    function($scope, $rootScope, $filter, Cluster, GlobalStack, UluwatuCluster) {
        var successEvents = ["REQUESTED",
            "CREATE_IN_PROGRESS",
            "START_REQUESTED",
            "START_IN_PROGRESS",
            "STOPPED",
            "STOP_REQUESTED",
            "STOP_IN_PROGRESS",
            "DELETE_IN_PROGRESS"
        ];

        var errorEvents = ["CLUSTER_CREATION_FAILED",
            "CREATE_FAILED",
            "START_FAILED",
            "DELETE_FAILED",
            "UPDATE_FAILED",
            "STOP_FAILED"
        ];

        var socket = io();
        socket.on('notification', handleNotification);

        function handleNotification(notification) {
            var eventType = notification.eventType;

            if (successEvents.indexOf(eventType) > -1) {
                $scope.showSuccess(notification.eventMessage, notification.stackName);
                handleStatusChange(notification, true);
            } else if (errorEvents.indexOf(eventType) > -1) {
                $scope.showErrorMessage(notification.eventMessage, notification.stackName);
                handleStatusChange(notification, true);
            } else {
                switch (eventType) {
                    case "DELETE_COMPLETED":
                        $scope.showSuccess(notification.eventMessage, notification.stackName);
                        var actCluster = $rootScope.activeCluster || {};
                        if (actCluster.id == notification.stackId) {
                            $rootScope.deselectActiveCluster();
                            $jq('.carousel').carousel(0);
                            $jq('#toggle-cluster-block-btn').removeClass('disabled');
                            $jq('#sort-clusters-btn').removeClass('disabled');
                            $jq('#create-cluster-btn').removeClass('disabled');
                            $jq("#notification-n-filtering").prop("disabled", false);
                        }
                        $rootScope.clusters = $filter('filter')($rootScope.clusters, function(value, index) {
                            return value.id != notification.stackId;
                        });
                        break;
                    case "IMAGE_COPY_STATE":
                        handleCopyStatus(notification);
                        break;
                    case "SMOKE_TEST_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.smoke_test_ambari_progress_state_label);
                        break;
                    case "INSTALL_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.install_ambari_progress_state_label);
                        break;
                    case "START_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.start_ambari_progress_state_label);
                        break;
                    case "STOP_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.stop_ambari_progress_state_label);
                        break;
                    case "RESTART_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.restart_ambari_progress_state_label);
                        break;
                    case "DECOMMISSION_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.decommission_ambari_progress_state_label);
                        break;
                    case "INSTALL_SERVICES_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.install_services_ambari_progress_state_label);
                        break;
                    case "START_SERVICES_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.start_services_ambari_progress_state_label);
                        break;
                    case "STOP_SERVICES_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.stop_services_ambari_progress_state_label);
                        break;
                    case "DECOMMISSION_SERVICES_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.decommission_services_ambari_progress_state_label);
                        break;
                    case "ENABLE_SERVICES_AMBARI_PROGRESS_STATE":
                        handleAmbariProgressState(notification, $rootScope.msg.enable_services_ambari_progress_state_label);
                        break;
                    case "AVAILABLE":
                        handleAvailableNotification(notification);
                        break;
                    case "UPTIME_NOTIFICATION":
                        handleUptimeNotification(notification);
                        break;
                    case "UPDATE_IN_PROGRESS":
                        handleUpdateInProgressNotification(notification);
                        handleStatusChange(notification, false);
                        break;
                }
            }

            $scope.$apply();
        }

        function getActCluster(notification) {
            return $filter('filter')($rootScope.clusters, {
                id: notification.stackId
            })[0];
        }

        function handleStatusChange(notification, refresh) {
            var actCluster = getActCluster(notification);
            if (actCluster == undefined) {
                UluwatuCluster.get(notification.stackId, function(cluster) {
                    actCluster = $filter('filter')($rootScope.clusters, {
                        id: notification.stackId
                    })[0];
                    if (actCluster == undefined) {
                        $rootScope.clusters.push(cluster);
                        $scope.$parent.orderClusters();
                    } else {
                        updateStatus(actCluster, notification, refresh);
                    }
                });
            } else {
                updateStatus(actCluster, notification, refresh);
            }
            addNotificationToGlobalEvents(notification);
        }

        function updateStatus(actCluster, notification, refresh) {
            actCluster.status = notification.stackStatus;
            actCluster.cluster = actCluster.cluster || {};
            actCluster.cluster.status = notification.clusterStatus;
            actCluster.cluster.statusReason = notification.eventMessage;
            if (refresh) {
                refreshMetadata(notification);
            }
        }

        function handleCopyStatus(notification) {
            var actCluster = getActCluster(notification);
            if (actCluster != undefined) {
                actCluster.copyState = notification.eventMessage;
            }
        }

        function handleAmbariProgressState(notification, message) {
            var actCluster = getActCluster(notification);
            if (actCluster != undefined) {
                actCluster.ambariProgressState = notification.eventMessage;
                actCluster.progressMessage = message;
            }
        }

        function handleAvailableNotification(notification) {
            var actCluster = getActCluster(notification);
            var msg = notification.eventMessage;
            var nodeCount = notification.nodeCount;
            if (nodeCount != null && nodeCount != undefined && nodeCount != 0) {
                actCluster.nodeCount = nodeCount;
            }
            updateStatus(actCluster, notification, true);
            $scope.showSuccess(msg, actCluster.name);
            addNotificationToGlobalEvents(notification);
            $rootScope.$broadcast('START_PERISCOPE_CLUSTER', actCluster, msg);
        }

        function handleUptimeNotification(notification) {
            var actCluster = getActCluster(notification);
            if (actCluster != undefined) {
                var SECONDS_PER_MINUTE = 60;
                var MILLIS_PER_SECOND = 1000;
                var runningInMs = parseInt(notification.eventMessage);
                var minutes = ((runningInMs / (MILLIS_PER_SECOND * SECONDS_PER_MINUTE)) % SECONDS_PER_MINUTE);
                var hours = (runningInMs / (MILLIS_PER_SECOND * SECONDS_PER_MINUTE * SECONDS_PER_MINUTE));
                actCluster.minutesUp = parseInt(minutes);
                actCluster.hoursUp = parseInt(hours);
            }
        }

        function handleUpdateInProgressNotification(notification) {
            var actCluster = getActCluster(notification);
            var msg = notification.eventMessage;
            var indexOfAmbariIp = msg.indexOf("Ambari ip:");
            if (actCluster != undefined && msg != null && msg != undefined && indexOfAmbariIp > -1) {
                if (actCluster.cluster == undefined) {
                    actCluster.cluster = {};
                }
                actCluster.cluster.ambariServerIp = msg.split(':')[1];
            }
            $scope.showSuccess(notification.eventMessage, notification.stackName);
        }

        function addNotificationToGlobalEvents(item) {
            item.customTimeStamp = new Date(item.eventTimestamp).toLocaleDateString() + " " + new Date(item.eventTimestamp).toLocaleTimeString();
            $rootScope.events.push(item);
        }

        function refreshMetadata(notification) {
            if ($rootScope.activeCluster.id != undefined && $rootScope.activeCluster.id == notification.stackId) {
                GlobalStack.get({
                    id: notification.stackId
                }, function(success) {
                    // refresh host metadata
                    if (success.cluster != null) {
                        $rootScope.activeCluster.cluster.status = success.cluster.status;
                        if (success.cluster.hostGroups != null) {
                            $rootScope.activeCluster.cluster.hostGroups = success.cluster.hostGroups;
                        }
                        if (success.cluster.serviceEndPoints != null) {
                            $rootScope.activeCluster.cluster.serviceEndPoints = success.cluster.serviceEndPoints;
                        }
                    }
                    // refresh instance metadata
                    var metadata = []
                    angular.forEach(success.instanceGroups, function(item) {
                        angular.forEach(item.metadata, function(item1) {
                            metadata.push(item1)
                            $rootScope.activeCluster.metadata = metadata // trigger activeCluster.metadata
                        });
                    });
                });
            }
        }

    }
]);