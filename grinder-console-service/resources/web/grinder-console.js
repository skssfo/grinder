jQuery(function($) {
    function addChangeDetection() {
        var changeables = $(".changeable");

        if (!changeables.length) {
            return;
        }

        $("label").each(function() {
            var l = this;

            if (l.htmlFor != '') {
                var e = $("#" + l.htmlFor)[0];

                if (e) {
                    e.label = this;
                } else {
                    $("[name='" + l.htmlFor + "']").each(function() {
                        this.label = l;
                    });
                }
            }
        });

        jQuery.fn.visible = function(show) {
            return this.css("visibility", show ? "visible" : "hidden");
        };

        var submit = $("#submit");
        submit.visible(false);

        changeables.each(function() {

            if (this.type === "checkbox") {
                this.modified = function() {
                    return this.checked != this.defaultChecked;
                };
            } else {
                this.original = this.value;
                this.modified = function() {
                    return this.original != this.value;
                };
            }

            $(this).change(function(e) {
                // This is wrong if multiple controls share the same label.
                if (e.target.modified()) {
                    $(e.target.label).addClass("changed");
                } else {
                    $(e.target.label).removeClass("changed");
                }

                submit.visible(changeables.filter(function(x) {
                    return this.modified();
                }).length);
            });
        });
    }

    function pollLiveData() {

        $(".live-data").each(function() {
            console.log("Registering " + this);
            var seq = -1;

            function poll(e) {
                // console.log("Polling " + e);
                $.get("/ui/poll", {k : e.id, s: seq}, function(x) {
                    // console.log("Update " + x);

                    $(e)
                    .stop()
                    .animate({opacity: 0.5},
                            "fast",
                            function() {
                                $(this).html(x.html);
                                $(this).animate({opacity: 1}, "fast");
                            });

                    seq = x.sequence;

                    // Dispatch in timer - directly calling poll()
                    // causes FF to spin sometimes.
                    setTimeout(function() {poll(e);}, 1);
                },
                "json");
            }

            poll(this);
        });
    }

    function buttons() {
        $(".grinder-button").each(function() {
            if (this.id) {

                var buttonOptions;

                if (this.classList.contains("grinder-button-icon")) {
                    buttonOptions = {
                            icons: { primary: this.id }
                    };
                }
                else {
                    buttonOptions = {};
                }

                $(this)
                .button(buttonOptions)
                .click(function() {
                    $.post("/ui/action/" + this.id);
                });
            }
            else {
                $(this).button();
            }
        });
    }

    buttons();
    addChangeDetection();
    pollLiveData();
});