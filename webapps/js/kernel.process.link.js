// "use strict";
/**
 * Process links.
 */
$$.process.link = new function () {
    const showForm = (uiid, id) => {
        $(`#${uiid} #linkEditor > form`).hide();
        $(`#${uiid} #linkEditor > form#${id}`).show();
    }

    /**
     * Sends checked request forms for adding links.
     * @param {*} uiid parent element with forms.
     * @param {*} requestUrl URL to load after adding to parent of uiid.
     */
    const add = (uiid, requestUrl) => {
        const deferreds = [];

        const forms = $('#' + uiid + ' form:visible');
        for (var i = 0; i < forms.length; i++) {
            const form = forms[i];
            if (form.check && form.check.checked)
                deferreds.push($$.ajax.post(form));
        }

        $.when.apply($, deferreds).done(() => { $$.ajax.load(requestUrl, $('#' + uiid).parent()) });
    }

    const customerRoleChanged = ($hidden) => {
        $hidden.closest('tr').find('form')[0].linkedObjectType.value = $hidden.val();
    }

    // public functions
    this.showForm = showForm;
    this.add = add;
    this.customerRoleChanged = customerRoleChanged;

    // $$.process.link.process
    this.process = new function () {
        /**
         * Creates a link process from pre-configured list.
         * @param {*} button
         * @param {*} returnUrl
         */
        const createAndLink = (button, returnUrl) => {
            $$.ajax.post(button.form).done((result) => {
                if (result.data.process.id > 0) {
                    $$.ajax.load(returnUrl, $(button.form).parent(), {control: button});
                } else {
                    // open with Wizard, not really tested for a long time!
                    const url = '/user/process.do?id=' + result.data.process.id + '&returnUrl=' + encodeURIComponent(returnUrl);
                    $$.ajax.load(url, $(button.form).parent(), {control: button});
                }
            });
        }

        const addExisting = (button) => {
            const form = button.form;

            form.action.value ='addExisting';

            let url = $$.ajax.formUrl(form);

            const processes = openedObjectList({typesInclude: ['process']});
            for (const i in processes) {
                const process = processes[i];
                url += '&bufferProcessId=' + process.id;
            }

            $$.ajax.load(url, $(form).parent())
        }

        // public functions
        this.createAndLink = createAndLink;
        this.addExisting = addExisting;
    }
}