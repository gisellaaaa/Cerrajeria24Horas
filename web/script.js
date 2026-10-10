(function () {
  "use strict";

  var anio = document.getElementById("anio");
  if (anio) {
    anio.textContent = String(new Date().getFullYear());
  }

  var cabecera = document.getElementById("cabecera");
  var botonMenu = document.querySelector(".hamburguesa");
  var menu = document.getElementById("menu");

  if (botonMenu && menu) {
    botonMenu.addEventListener("click", function () {
      var abierto = menu.classList.toggle("menu--abierto");
      botonMenu.setAttribute("aria-expanded", abierto ? "true" : "false");
      botonMenu.setAttribute("aria-label", abierto ? "Cerrar menú" : "Abrir menú");
    });

    menu.addEventListener("click", function (evento) {
      if (evento.target.tagName === "A") {
        menu.classList.remove("menu--abierto");
        botonMenu.setAttribute("aria-expanded", "false");
        botonMenu.setAttribute("aria-label", "Abrir menú");
      }
    });
  }

  if (cabecera) {
    var actualizarCabecera = function () {
      if (window.scrollY > 8) {
        cabecera.classList.add("cabecera--compacta");
      } else {
        cabecera.classList.remove("cabecera--compacta");
      }
    };
    actualizarCabecera();
    window.addEventListener("scroll", actualizarCabecera, { passive: true });
  }
})();
