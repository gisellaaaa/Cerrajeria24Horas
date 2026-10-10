(function () {
  "use strict";

  var anio = document.getElementById("anio");
  if (anio) {
    anio.textContent = String(new Date().getFullYear());
  }

  /* Cabecera: sombra al desplazarse */
  var cabecera = document.getElementById("cabecera");
  if (cabecera) {
    var actualizarCabecera = function () {
      cabecera.classList.toggle("cabecera--compacta", window.scrollY > 8);
    };
    actualizarCabecera();
    window.addEventListener("scroll", actualizarCabecera, { passive: true });
  }

  /* Volver arriba */
  var botonArriba = document.getElementById("arriba");
  if (botonArriba) {
    var actualizarArriba = function () {
      botonArriba.classList.toggle("visible", window.scrollY > 480);
    };
    actualizarArriba();
    window.addEventListener("scroll", actualizarArriba, { passive: true });
  }

  /* Resaltar el enlace activo según la sección visible */
  var enlacesNav = Array.prototype.slice.call(document.querySelectorAll(".navbar__links a"));
  var secciones = enlacesNav
    .map(function (enlace) {
      var id = enlace.getAttribute("href");
      return id && id.charAt(0) === "#" ? document.querySelector(id) : null;
    })
    .filter(Boolean);

  if (secciones.length) {
    var marcarActivo = function () {
      var posicion = window.scrollY + 150;
      var actual = secciones[0];
      secciones.forEach(function (seccion) {
        if (seccion.offsetTop <= posicion) {
          actual = seccion;
        }
      });
      enlacesNav.forEach(function (enlace) {
        enlace.classList.toggle("activo", enlace.getAttribute("href") === "#" + actual.id);
      });
    };
    marcarActivo();
    window.addEventListener("scroll", marcarActivo, { passive: true });
  }

  /* Aparición suave de bloques al hacer scroll */
  var bloques = document.querySelectorAll(".seccion, .servicio-item, .video-tarjeta, .pie__col");
  if ("IntersectionObserver" in window && bloques.length) {
    var observador = new IntersectionObserver(function (entradas) {
      entradas.forEach(function (entrada) {
        if (entrada.isIntersecting) {
          entrada.target.classList.add("visible");
          observador.unobserve(entrada.target);
        }
      });
    }, { rootMargin: "0px 0px -80px 0px", threshold: 0.08 });

    Array.prototype.forEach.call(bloques, function (bloque) {
      bloque.classList.add("revela");
      observador.observe(bloque);
    });
  }

  /* Menú móvil */
  var botonMenu = document.querySelector(".hamburguesa");
  var links = document.getElementById("navbarLinks");

  if (botonMenu && links) {
    botonMenu.addEventListener("click", function () {
      var abierto = links.classList.toggle("navbar__links--abierto");
      botonMenu.setAttribute("aria-expanded", abierto ? "true" : "false");
      botonMenu.setAttribute("aria-label", abierto ? "Cerrar menú" : "Abrir menú");
    });

    links.addEventListener("click", function (evento) {
      if (evento.target.tagName === "A") {
        links.classList.remove("navbar__links--abierto");
        botonMenu.setAttribute("aria-expanded", "false");
        botonMenu.setAttribute("aria-label", "Abrir menú");
      }
    });
  }

  /* Formulario de programación -> WhatsApp */
  var formulario = document.getElementById("formServicio");
  var estado = document.getElementById("pestado");

  if (formulario) {
    formulario.addEventListener("submit", function (evento) {
      evento.preventDefault();

      var nombres = document.getElementById("nombres").value.trim();
      var fecha = document.getElementById("fecha").value;
      var hora = document.getElementById("hora").value;
      var direccion = document.getElementById("direccion").value.trim();
      var servicio = document.getElementById("select1").value;

      if (!nombres || !direccion || !servicio) {
        if (estado) {
          estado.textContent = "Completa tu nombre, la dirección y el tipo de servicio.";
        }
        return;
      }

      var mensaje =
        "Nombres y apellidos: " + nombres + "\n" +
        "Fecha de servicio: " + (fecha || "por definir") + "\n" +
        "Hora: " + (hora || "por definir") + "\n" +
        "Dirección: " + direccion + "\n" +
        "Tipo de servicio: " + servicio;

      var url = "https://wa.me/573183850484?text=" + encodeURIComponent(mensaje);
      window.open(url, "_blank");

      if (estado) {
        estado.textContent = "Abriendo WhatsApp para enviar tu solicitud…";
      }
    });
  }

  /* Videos demostrativos: reproducir al pulsar la portada */
  var tarjetas = document.querySelectorAll(".video-tarjeta");

  Array.prototype.forEach.call(tarjetas, function (tarjeta) {
    var video = tarjeta.querySelector("video");
    var overlay = tarjeta.querySelector(".video-overlay");

    if (!video || !overlay) {
      return;
    }

    video.controls = false;

    overlay.addEventListener("click", function () {
      if (!video.paused) {
        return;
      }

      Array.prototype.forEach.call(document.querySelectorAll("video"), function (otro) {
        if (otro !== video && !otro.paused) {
          otro.pause();
          otro.controls = false;
          var contenedor = otro.closest(".video-tarjeta");
          if (contenedor) {
            contenedor.classList.remove("reproduciendo");
          }
        }
      });

      var reproduccion = video.play();
      if (reproduccion && typeof reproduccion.then === "function") {
        reproduccion.then(function () {
          video.controls = true;
          tarjeta.classList.add("reproduciendo");
        }).catch(function () {
          video.muted = true;
          video.play().then(function () {
            video.controls = true;
            tarjeta.classList.add("reproduciendo");
          });
        });
      } else {
        video.controls = true;
        tarjeta.classList.add("reproduciendo");
      }
    });

    var detener = function () {
      tarjeta.classList.remove("reproduciendo");
      video.controls = false;
    };

    video.addEventListener("pause", detener);
    video.addEventListener("ended", detener);
  });
})();
