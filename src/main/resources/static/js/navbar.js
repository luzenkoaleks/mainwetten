document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.querySelector(".app-nav-toggle");
    const navigation = document.querySelector(".app-nav-collapsible");

    if (!toggle || !navigation) {
        return;
    }

    let lastScrollY = window.scrollY;
    let ignoreScrollUntil = 0;

    const isMenuOpen = () =>
        toggle.getAttribute("aria-expanded") === "true";

    const setMenuOpen = (open) => {
        navigation.classList.toggle("is-open", open);
        toggle.setAttribute("aria-expanded", String(open));
        toggle.setAttribute(
            "aria-label",
            open ? "Navigation schließen" : "Navigation öffnen"
        );

        if (open) {
            /*
             * Das Öffnen verändert die Höhe der Navbar und kann dadurch
             * selbst ein kleines Scroll-Ereignis auslösen.
             */
            ignoreScrollUntil = Date.now() + 350;
            lastScrollY = window.scrollY;
        }
    };

    toggle.addEventListener("click", () => {
        setMenuOpen(!isMenuOpen());
    });

    navigation.querySelectorAll("a").forEach((link) => {
        link.addEventListener("click", () => {
            setMenuOpen(false);
        });
    });

    window.addEventListener(
        "scroll",
        () => {
            const currentScrollY = window.scrollY;
            const scrollDifference = currentScrollY - lastScrollY;

            if (
                window.innerWidth <= 575 &&
                isMenuOpen() &&
                Date.now() > ignoreScrollUntil &&
                currentScrollY > 40 &&
                scrollDifference > 12
            ) {
                setMenuOpen(false);
            }

            lastScrollY = currentScrollY;
        },
        { passive: true }
    );

    window.addEventListener("resize", () => {
        if (window.innerWidth > 575) {
            setMenuOpen(false);
        }
    });
});