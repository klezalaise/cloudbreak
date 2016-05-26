</form>
<div class="panel-group" id="wap-net-accordion" role="tablist" aria-multiselectable="true">
    <div class="panel panel-default">
        <div class="panel-heading" role="tab" id="wap-net-headingOne">
            <h4 class="panel-title">
                <a class="collapsed" role="button" data-toggle="collapse" data-parent="#wap-net-accordion" href="#wap-net-collapseOne" aria-expanded="true" aria-controls="wap-net-collapseOne" ng-click="selectWapNetworkType1()">
                <i class="fa fa-sitemap fa-fw"/><span style="padding-left: 10px">{{msg.network_wap_form_type1_title}}</span>
                </a>
            </h4>
        </div>
        <div id="wap-net-collapseOne" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="wap-net-headingOne">
            <div class="panel-body">
                <div class="form-group col-sm-12">
                    <i class="fa fa-info-circle" /><small>&nbsp;{{msg.network_wap_form_type1_info}}</small>
                </div>
                <form class="form-horizontal" role="form" name="wapNetworkForm_1">
                    <div class="form-group" ng-class="{ 'has-error': wapNetworkForm_1.wap_networkName.$dirty && wapNetworkForm_1.wap_networkName.$invalid }">
                        <label class="col-sm-3 control-label" for="wap_networkName">{{msg.name_label}}</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control" ng-pattern="/^[a-zA-Z][-a-zA-Z0-9]*$/" name="wap_networkName" ng-model="network.name" ng-minlength="5" ng-maxlength="100" required id="wap_networkName" placeholder="{{msg.name_placeholder}}">
                            <div class="help-block" ng-show="wapNetworkForm_1.wap_networkName.$dirty && wapNetworkForm_1.wap_networkName.$invalid">
                                <i class="fa fa-warning"></i> {{msg.network_name_invalid}}
                            </div>
                        </div>
                        <!-- .col-sm-9 -->
                    </div>
                    <div class="form-group" ng-class="{ 'has-error': wapNetworkForm_1.wap_networkDescription.$dirty && wapNetworkForm_1.wap_networkDescription.$invalid }">
                        <label class="col-sm-3 control-label" for="wap_networkDescription">{{msg.description_label}}</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control" name="wap_networkDescription" ng-model="network.description" ng-maxlength="1000" id="wap_networkDescription" placeholder="{{msg.network_form_description_placeholder}}">
                            <div class="help-block" ng-show="wapNetworkForm_1.gcp_networkDescription.$dirty && wapNetworkForm_1.wap_networkDescription.$invalid">
                                <i class="fa fa-warning"></i> {{msg.netowrk_description_invalid}}
                            </div>
                        </div>
                        <!-- .col-sm-9 -->
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label" for="wap_network_public">{{msg.public_in_account_label}}</label>
                        <div class="col-sm-9">
                            <input type="checkbox" name="wap_network_public" id="wap_network_public" ng-model="network.publicInAccount">
                        </div>
                        <!-- .col-sm-9 -->
                    </div>
                    <div class="form-group">
                        <label class="col-sm-3 control-label" for="topologySelect">{{msg.credential_select_topology}}</label>
                        <div class="col-sm-9">
                            <select class="form-control" id="topologySelect" name="topologySelect" ng-model="network.topologyId" ng-options="topology.id as topology.name for topology in $root.topologies | filter: filterByCloudPlatform | orderBy:'name'">
                                <option value="">-- {{msg.credential_select_topology.toLowerCase()}} --</option>
                            </select>
                        </div>
                        <!-- .col-sm-9 -->
                    </div>
                    <div class="row btn-row">
                        <div class="col-sm-9 col-sm-offset-3">
                            <a id="createWapNetwork_1" ng-disabled="wapNetworkForm_1.$invalid" class="btn btn-success btn-block" ng-click="createWapNetwork()" role="button"><i class="fa fa-plus fa-fw"></i>
                            {{msg.network_form_create}}</a>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
<form>
