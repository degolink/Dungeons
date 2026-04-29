function withAnime(callback) {
    if (typeof window === 'undefined' || typeof window.anime !== 'function') {
        return;
    }

    callback(window.anime);
}

function prefersReducedMotion() {
    return typeof window !== 'undefined'
        && typeof window.matchMedia === 'function'
        && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

function shouldAnimate() {
    return !prefersReducedMotion();
}

function setAnimationReady(selector) {
    document.querySelectorAll(selector).forEach(element => {
        element.style.opacity = '1';
    });
}

function animatePageIntro() {
    const animatedSelectors = [
        '[data-anim-page]',
        '[data-anim-hero]',
        '[data-anim-panel]',
        '[data-anim-card]',
        '[data-anim-stagger]'
    ];

    if (!shouldAnimate()) {
        animatedSelectors.forEach(setAnimationReady);
        return;
    }

    withAnime(anime => {
        anime({
            targets: '[data-anim-page]',
            opacity: [0, 1],
            translateY: [22, 0],
            duration: 520,
            easing: 'easeOutCubic'
        });

        anime({
            targets: '[data-anim-hero]',
            opacity: [0, 1],
            translateY: [26, 0],
            scale: [0.98, 1],
            duration: 560,
            delay: anime.stagger(90),
            easing: 'easeOutExpo'
        });

        anime({
            targets: '[data-anim-panel]',
            opacity: [0, 1],
            translateY: [20, 0],
            duration: 440,
            delay: anime.stagger(70, { start: 110 }),
            easing: 'easeOutQuad'
        });

        anime({
            targets: '[data-anim-card]',
            opacity: [0, 1],
            translateY: [18, 0],
            scale: [0.985, 1],
            duration: 380,
            delay: anime.stagger(40, { start: 180 }),
            easing: 'easeOutQuad'
        });

        anime({
            targets: '[data-anim-stagger]',
            opacity: [0, 1],
            translateY: [12, 0],
            duration: 300,
            delay: anime.stagger(55, { start: 220 }),
            easing: 'easeOutQuad'
        });
    });
}

function animateTurnIndicators() {
    if (!shouldAnimate()) {
        return;
    }

    withAnime(anime => {
        anime.remove('[data-turn-indicator="true"]');
        anime({
            targets: '[data-turn-indicator="true"]',
            scale: [1, 1.03, 1],
            duration: 1500,
            delay: 450,
            easing: 'easeInOutSine',
            loop: true
        });
    });
}

function initInteractiveAnimations() {
    if (!shouldAnimate()) {
        return;
    }

    document.querySelectorAll('[data-anim-hover]').forEach(element => {
        if (element.dataset.animHoverBound === 'true') {
            return;
        }

        element.dataset.animHoverBound = 'true';

        element.addEventListener('mouseenter', () => {
            withAnime(anime => {
                anime.remove(element);
                anime({
                    targets: element,
                    translateY: -2,
                    scale: 1.01,
                    duration: 180,
                    easing: 'easeOutQuad'
                });
            });
        });

        element.addEventListener('mouseleave', () => {
            withAnime(anime => {
                anime.remove(element);
                anime({
                    targets: element,
                    translateY: 0,
                    scale: 1,
                    duration: 180,
                    easing: 'easeOutQuad'
                });
            });
        });
    });
}

function openDrawerPanel(drawerId, panelSelector = '[data-drawer-panel]') {
    const drawer = document.getElementById(drawerId);
    if (!drawer) {
        return;
    }

    drawer.classList.remove('hidden');
    document.body.classList.add('overflow-hidden');

    if (!shouldAnimate()) {
        return;
    }

    const panel = drawer.querySelector(panelSelector);
    const overlay = drawer.querySelector('[data-drawer-overlay]');

    withAnime(anime => {
        if (overlay) {
            anime.remove(overlay);
            anime({
                targets: overlay,
                opacity: [0, 1],
                duration: 180,
                easing: 'easeOutQuad'
            });
        }

        if (panel) {
            anime.remove(panel);
            anime({
                targets: panel,
                translateX: [42, 0],
                opacity: [0.4, 1],
                duration: 260,
                easing: 'easeOutCubic'
            });
        }
    });
}

function closeDrawerPanel(drawerId, panelSelector = '[data-drawer-panel]') {
    const drawer = document.getElementById(drawerId);
    if (!drawer) {
        return;
    }

    const hideDrawer = () => {
        drawer.classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
    };

    if (!shouldAnimate()) {
        hideDrawer();
        return;
    }

    const panel = drawer.querySelector(panelSelector);
    const overlay = drawer.querySelector('[data-drawer-overlay]');

    let completed = 0;
    const expected = (panel ? 1 : 0) + (overlay ? 1 : 0) || 1;
    const done = () => {
        completed += 1;
        if (completed >= expected) {
            hideDrawer();
        }
    };

    withAnime(anime => {
        if (overlay) {
            anime.remove(overlay);
            anime({
                targets: overlay,
                opacity: [1, 0],
                duration: 160,
                easing: 'easeInQuad',
                complete: done
            });
        }

        if (panel) {
            anime.remove(panel);
            anime({
                targets: panel,
                translateX: [0, 36],
                opacity: [1, 0.4],
                duration: 220,
                easing: 'easeInQuad',
                complete: done
            });
        }

        if (!overlay && !panel) {
            done();
        }
    });
}

function showToast(message, isSuccess = true, toastId = 'toast') {
    const toast = document.getElementById(toastId);
    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.remove('bg-red-600', 'bg-green-600');
    toast.classList.add(isSuccess ? 'bg-green-600' : 'bg-red-600');
    toast.classList.add('opacity-100');

    withAnime(anime => {
        anime.remove(toast);
        anime({
            targets: toast,
            translateY: [20, 0],
            scale: [0.96, 1],
            opacity: [0, 1],
            duration: 280,
            easing: 'easeOutQuad'
        });
    });

    if (toast._hideTimeout) {
        window.clearTimeout(toast._hideTimeout);
    }

    toast._hideTimeout = window.setTimeout(() => {
        let animated = false;
        withAnime(anime => {
            animated = true;
            anime({
                targets: toast,
                translateY: [0, 10],
                opacity: [1, 0],
                duration: 220,
                easing: 'easeInQuad',
                complete: () => toast.classList.remove('opacity-100')
            });
        });

        if (!animated) {
            toast.classList.remove('opacity-100');
        }
    }, 2200);
}

function prependLogItem(logId, text, itemClass = 'bg-slate-800 border border-slate-700 rounded p-2') {
    const log = document.getElementById(logId);
    if (!log || !text) {
        return;
    }

    const item = document.createElement('li');
    item.className = itemClass;
    item.textContent = text;
    log.prepend(item);

    withAnime(anime => {
        anime({
            targets: item,
            opacity: [0, 1],
            translateY: [-12, 0],
            duration: 320,
            easing: 'easeOutQuad'
        });
    });
}

function animateValueChange(element) {
    if (!element) {
        return;
    }

    withAnime(anime => {
        anime.remove(element);
        anime({
            targets: element,
            scale: [1, 1.14, 1],
            duration: 380,
            easing: 'easeOutBack'
        });
    });
}

function animateSelectionCard(element) {
    if (!element) {
        return;
    }

    element.style.opacity = '1';

    withAnime(anime => {
        anime({
            targets: element,
            scale: [1, 1.02, 1],
            duration: 320,
            easing: 'easeOutBack'
        });
    });
}

function animateBattleImpact(target, accentColor = 'rgba(248, 113, 113, 0.55)') {
    if (!target) {
        return;
    }

    withAnime(anime => {
        anime.remove(target);
        anime({
            targets: target,
            keyframes: [
                { scale: 1.02, boxShadow: `0 0 0 0 ${accentColor}` },
                { scale: 1, boxShadow: `0 0 22px 4px ${accentColor}` },
                { scale: 1, boxShadow: '0 0 0 0 rgba(0,0,0,0)' }
            ],
            duration: 420,
            easing: 'easeOutQuad'
        });
    });
}

function parseCombatEventDetails(evento = '') {
    const texto = typeof evento === 'string' ? evento : '';
    const damageMatch = texto.match(/infligió\s+(\d+)/i);

    return {
        damage: damageMatch ? Number(damageMatch[1]) : null,
        critical: /cr[ií]tico/i.test(texto),
        blocked: /bloqueado/i.test(texto)
    };
}

function ensureFxAnchor(element) {
    if (!element || typeof window === 'undefined') {
        return;
    }

    if (window.getComputedStyle(element).position === 'static') {
        element.style.position = 'relative';
    }
}

function spawnFloatingCombatText(target, text, critical = false, blocked = false) {
    if (!target || !text) {
        return;
    }

    ensureFxAnchor(target);

    const popup = document.createElement('div');
    popup.textContent = text;
    popup.className = 'pointer-events-none select-none';
    popup.style.position = 'absolute';
    popup.style.right = '0.75rem';
    popup.style.top = '0.35rem';
    popup.style.zIndex = '40';
    popup.style.fontFamily = "'Press Start 2P', cursive";
    popup.style.fontSize = critical ? '0.7rem' : '0.62rem';
    popup.style.lineHeight = '1.25';
    popup.style.whiteSpace = 'nowrap';
    popup.style.color = blocked ? '#93c5fd' : (critical ? '#fca5a5' : '#fde68a');
    popup.style.textShadow = blocked
        ? '0 0 10px rgba(59, 130, 246, 0.7)'
        : (critical ? '0 0 12px rgba(248, 113, 113, 0.8)' : '0 0 10px rgba(250, 204, 21, 0.75)');

    target.appendChild(popup);

    withAnime(anime => {
        anime({
            targets: popup,
            translateY: [8, -28],
            opacity: [0, 1, 1, 0],
            scale: critical ? [0.75, 1.08, 1] : [0.85, 1, 1],
            duration: critical ? 900 : 720,
            easing: 'easeOutExpo',
            complete: () => popup.remove()
        });
    });
}

function spawnHitSpark(target, variant = 'sword', critical = false) {
    if (!target) {
        return;
    }

    ensureFxAnchor(target);

    const spark = document.createElement('div');
    spark.className = 'pointer-events-none';
    spark.style.position = 'absolute';
    spark.style.left = '50%';
    spark.style.top = '50%';
    spark.style.width = critical ? '92px' : '72px';
    spark.style.height = critical ? '92px' : '72px';
    spark.style.borderRadius = '9999px';
    spark.style.transform = 'translate(-50%, -50%)';
    spark.style.zIndex = '35';
    spark.style.mixBlendMode = 'screen';

    const trail = document.createElement('div');
    trail.className = 'pointer-events-none';
    trail.style.position = 'absolute';
    trail.style.left = '50%';
    trail.style.top = '50%';
    trail.style.transform = 'translate(-50%, -50%)';
    trail.style.zIndex = '36';
    trail.style.mixBlendMode = 'screen';

    if (variant === 'axe') {
        spark.style.background = 'radial-gradient(circle, rgba(254,215,170,0.98) 0%, rgba(251,146,60,0.75) 32%, rgba(248,113,113,0.28) 66%, rgba(255,255,255,0) 76%)';
        trail.style.width = critical ? '58px' : '46px';
        trail.style.height = critical ? '58px' : '46px';
        trail.style.border = '3px solid rgba(255,237,213,0.85)';
        trail.style.borderTopColor = 'transparent';
        trail.style.borderLeftColor = 'transparent';
        trail.style.borderRadius = '9999px';
        trail.style.rotate = '-24deg';
    } else if (variant === 'fist') {
        spark.style.background = 'radial-gradient(circle, rgba(191,219,254,0.98) 0%, rgba(96,165,250,0.72) 34%, rgba(34,197,94,0.22) 66%, rgba(255,255,255,0) 76%)';
        trail.style.width = critical ? '44px' : '36px';
        trail.style.height = '12px';
        trail.style.borderRadius = '9999px';
        trail.style.background = 'linear-gradient(90deg, rgba(255,255,255,0), rgba(191,219,254,0.95), rgba(255,255,255,0))';
    } else {
        spark.style.background = 'radial-gradient(circle, rgba(255,255,255,0.98) 0%, rgba(250,204,21,0.78) 30%, rgba(248,113,113,0.3) 64%, rgba(255,255,255,0) 76%)';
        trail.style.width = critical ? '108px' : '86px';
        trail.style.height = '5px';
        trail.style.borderRadius = '9999px';
        trail.style.background = 'linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.96), rgba(250,204,21,0.85), rgba(255,255,255,0))';
        trail.style.rotate = '-24deg';
    }

    target.appendChild(spark);
    target.appendChild(trail);

    withAnime(anime => {
        anime({
            targets: spark,
            scale: [0.4, critical ? 1.45 : 1.2],
            opacity: [1, 0],
            duration: critical ? 280 : 190,
            easing: 'easeOutExpo',
            complete: () => spark.remove()
        });

        anime({
            targets: trail,
            scaleX: [0.35, 1.2],
            scaleY: [0.8, critical ? 1.3 : 1],
            opacity: [1, 0],
            duration: critical ? 240 : 170,
            easing: 'easeOutExpo',
            complete: () => trail.remove()
        });
    });
}

function animateSceneShake(scene, intensity = 8, duration = 180) {
    if (!scene) {
        return;
    }

    withAnime(anime => {
        anime.remove(scene);
        anime({
            targets: scene,
            keyframes: [
                { translateX: intensity, translateY: -1 },
                { translateX: -intensity * 0.85, translateY: 1 },
                { translateX: intensity * 0.5, translateY: -1 },
                { translateX: 0, translateY: 0 }
            ],
            duration,
            easing: 'easeInOutSine'
        });
    });
}

function flashBattleScene(scene, variant = 'sword', critical = false) {
    if (!scene) {
        return;
    }

    ensureFxAnchor(scene);

    const overlay = document.createElement('div');
    overlay.className = 'pointer-events-none absolute inset-0';
    overlay.style.zIndex = '24';
    overlay.style.borderRadius = 'inherit';
    overlay.style.opacity = '0';
    overlay.style.mixBlendMode = 'screen';
    overlay.style.background = variant === 'axe'
        ? 'radial-gradient(circle at center, rgba(251,146,60,0.28), rgba(127,29,29,0.18) 48%, rgba(255,255,255,0) 78%)'
        : (variant === 'fist'
            ? 'radial-gradient(circle at center, rgba(96,165,250,0.24), rgba(15,23,42,0.12) 48%, rgba(255,255,255,0) 78%)'
            : 'radial-gradient(circle at center, rgba(250,204,21,0.24), rgba(248,113,113,0.16) 48%, rgba(255,255,255,0) 78%)');

    const slash = document.createElement('div');
    slash.className = 'pointer-events-none absolute';
    slash.style.left = '50%';
    slash.style.top = '50%';
    slash.style.width = critical ? '220px' : '168px';
    slash.style.height = variant === 'axe' ? '12px' : '8px';
    slash.style.borderRadius = '9999px';
    slash.style.transform = 'translate(-50%, -50%)';
    slash.style.rotate = variant === 'fist' ? '0deg' : '-18deg';
    slash.style.zIndex = '25';
    slash.style.opacity = '0';
    slash.style.background = variant === 'axe'
        ? 'linear-gradient(90deg, rgba(255,255,255,0), rgba(254,215,170,0.95), rgba(251,146,60,0.88), rgba(255,255,255,0))'
        : (variant === 'fist'
            ? 'linear-gradient(90deg, rgba(255,255,255,0), rgba(191,219,254,0.98), rgba(96,165,250,0.85), rgba(255,255,255,0))'
            : 'linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.98), rgba(250,204,21,0.88), rgba(255,255,255,0))');
    slash.style.boxShadow = critical ? '0 0 16px rgba(255,255,255,0.55)' : '0 0 10px rgba(255,255,255,0.35)';

    scene.appendChild(overlay);
    scene.appendChild(slash);

    withAnime(anime => {
        anime({
            targets: overlay,
            opacity: [0, critical ? 0.95 : 0.6, 0],
            duration: critical ? 260 : 180,
            easing: 'easeOutQuad',
            complete: () => overlay.remove()
        });

        anime({
            targets: slash,
            scaleX: [0.3, 1.12],
            scaleY: [0.7, critical ? 1.35 : 1],
            opacity: [0, 1, 0],
            duration: critical ? 280 : 190,
            easing: 'easeOutExpo',
            complete: () => slash.remove()
        });
    });
}

function animateMeleeExchange({
    attacker,
    target,
    scene,
    damage = null,
    critical = false,
    blocked = false,
    variant = 'sword',
    onImpact
} = {}) {
    let impacted = false;
    const impact = () => {
        if (impacted) {
            return;
        }
        impacted = true;

        if (typeof onImpact === 'function') {
            onImpact();
        }

        const accent = variant === 'axe'
            ? 'rgba(251, 146, 60, 0.55)'
            : (variant === 'fist' ? 'rgba(96, 165, 250, 0.5)' : 'rgba(250, 204, 21, 0.55)');
        const shakeIntensity = critical ? 13 : (variant === 'axe' ? 10 : (variant === 'fist' ? 6 : 8));
        const text = blocked
            ? 'BLOQUEADO 🛡️'
            : (critical ? `CRÍTICO 💥 ${damage ?? ''}`.trim() : (damage != null ? `-${damage}` : '¡Golpe!'));

        spawnHitSpark(target, variant, critical);
        spawnFloatingCombatText(target, text, critical, blocked);
        flashBattleScene(scene || target, variant, critical);
        animateSceneShake(scene || target, shakeIntensity, critical ? 260 : 180);
        animateBattleImpact(target, accent);
    };

    if (!attacker || !target) {
        impact();
        return Promise.resolve();
    }

    return new Promise(resolve => {
        let animated = false;

        withAnime(anime => {
            animated = true;

            const attackerBox = attacker.getBoundingClientRect();
            const targetBox = target.getBoundingClientRect();
            const direction = attackerBox.left <= targetBox.left ? 1 : -1;
            const windUp = variant === 'axe' ? 16 : (variant === 'fist' ? 8 : 12);
            const dash = variant === 'axe' ? 30 : (variant === 'fist' ? 18 : 24);
            const recoil = critical ? 18 : (variant === 'axe' ? 14 : (variant === 'fist' ? 8 : 12));
            const rotation = variant === 'axe' ? 8 : (variant === 'sword' ? 5 : 2);

            anime.remove(attacker);
            anime.remove(target);

            anime.timeline({ complete: resolve })
                .add({
                    targets: attacker,
                    translateX: [0, -direction * windUp],
                    rotate: [0, -direction * rotation],
                    duration: 120,
                    easing: 'easeOutQuad'
                })
                .add({
                    targets: attacker,
                    translateX: [-direction * windUp, direction * dash],
                    rotate: [-direction * rotation, direction * (rotation / 2)],
                    duration: variant === 'axe' ? 140 : 100,
                    easing: 'easeInQuad'
                })
                .add({
                    targets: [attacker, target],
                    duration: critical ? 90 : 55,
                    easing: 'linear',
                    begin: impact
                })
                .add({
                    targets: target,
                    translateX: [0, direction * recoil, 0],
                    scale: [1, critical ? 1.03 : 1.02, 1],
                    duration: critical ? 280 : 200,
                    easing: 'easeOutExpo'
                })
                .add({
                    targets: attacker,
                    translateX: [direction * dash, 0],
                    rotate: [direction * (rotation / 2), 0],
                    duration: 180,
                    easing: 'easeOutQuad'
                }, '-=220');
        });

        if (!animated) {
            impact();
            resolve();
        }
    });
}

function animateCombatEntrance() {
    animatePageIntro();
    animateTurnIndicators();
}

function openModalById(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        return;
    }

    modal.classList.remove('hidden');
    modal.classList.add('flex');
    document.body.classList.add('overflow-hidden');

    const panel = modal.querySelector('[data-modal-panel]') || modal.querySelector('.relative');
    withAnime(anime => {
        if (!panel) {
            return;
        }

        anime.remove(panel);
        anime({
            targets: panel,
            opacity: [0, 1],
            translateY: [16, 0],
            scale: [0.98, 1],
            duration: 260,
            easing: 'easeOutQuad'
        });
    });
}

function closeModalById(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) {
        return;
    }

    modal.classList.add('hidden');
    modal.classList.remove('flex');

    const hasOpenModal = Array.from(document.querySelectorAll('[data-modal-root]'))
        .some(item => !item.classList.contains('hidden'));

    if (!hasOpenModal) {
        document.body.classList.remove('overflow-hidden');
    }
}

document.addEventListener('click', event => {
    const openTrigger = event.target.closest('[data-modal-open]');
    if (openTrigger) {
        event.preventDefault();
        openModalById(openTrigger.getAttribute('data-modal-open'));
        return;
    }

    const closeTrigger = event.target.closest('[data-modal-close]');
    if (closeTrigger) {
        event.preventDefault();
        closeModalById(closeTrigger.getAttribute('data-modal-close'));
        return;
    }

    const overlay = event.target.closest('[data-modal-overlay]');
    if (overlay && event.target === overlay) {
        closeModalById(overlay.getAttribute('data-modal-overlay'));
    }
});

document.addEventListener('keydown', event => {
    if (event.key !== 'Escape') {
        return;
    }

    document.querySelectorAll('[data-modal-root]').forEach(modal => {
        if (!modal.classList.contains('hidden')) {
            closeModalById(modal.id);
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    animateCombatEntrance();
    initInteractiveAnimations();
});
