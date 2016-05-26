<form class="form-horizontal" role="document">
    <!-- role: 'document' - non-editable "form" -->

    <div class="form-group">
        <label class="col-sm-3 control-label" for="wapname">{{msg.name_label}}</label>

        <div class="col-sm-9">
            <p id="wapname" class="form-control-static">{{credential.name}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>

    <div class="form-group" ng-show="credential.description">
        <label class="col-sm-3 control-label" for="wapdescriptionfield">{{msg.description_label}}</label>

        <div class="col-sm-9">
            <p id="wapdescriptionfield" class="form-control-static">{{credential.description}}</p>
        </div>
        <!-- .col-sm-9 -->
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label" for="wapsubscriptionId">{{msg.credential_wap_form_subscription_id_label}}</label>

        <div class="col-sm-9">
            <p id="gcpclsubscriptionId" class="form-control-static">{{credential.parameters.subscriptionId}}</p>
        </div>

        <!-- .col-sm-9 -->

</div>
    <div class="form-group">
        <label class="col-sm-3 control-label" for="gcplprojectId">{{msg.credential_wap_form_certificate_label}}</label>

        <div class="col-sm-9">
            <p id="gcplprojectId" class="form-control-static">{{credential.parameters.certificate}}</p>
        </div>

        <!-- .col-sm-9 -->
    </div>
</form>
