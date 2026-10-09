drop database if exists Prueba;
-- =============================================================================
-- TECHNOVA SOLUTIONS - BASE DE DATOS OPTIMIZADA CON PERMISOS (RBAC)
-- RDBMS: PostgreSQL
-- =============================================================================

DROP TABLE IF EXISTS COMMENT CASCADE;
DROP TABLE IF EXISTS TICKET_HISTORY CASCADE;
DROP TABLE IF EXISTS TICKET_ASSIGNMENT CASCADE;
DROP TABLE IF EXISTS STATUS_TRANSITION CASCADE;
DROP TABLE IF EXISTS TICKET CASCADE;
DROP TABLE IF EXISTS TICKET_STATUS CASCADE;
DROP TABLE IF EXISTS PRIORITY CASCADE;
DROP TABLE IF EXISTS CATEGORY CASCADE;
DROP TABLE IF EXISTS ROLE_PERMISSION CASCADE; -- Nueva tabla intermedia
DROP TABLE IF EXISTS PERMISSION CASCADE;     -- Nueva tabla catálogo de permisos
DROP TABLE IF EXISTS USER_ROLE CASCADE;
DROP TABLE IF EXISTS ROLE CASCADE;
DROP TABLE IF EXISTS USER_ACCOUNT CASCADE;

-- -----------------------------------------------------------------------------
-- 1. USUARIOS, ROLES Y PERMISOS (RBAC - RF-01, RF-02)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS USER_ACCOUNT (
                              id SERIAL PRIMARY KEY,
                              first_name VARCHAR(100) NOT NULL,
                              last_name VARCHAR(100) NOT NULL,
                              email VARCHAR(150) NOT NULL UNIQUE,
                              password_hash VARCHAR(255) NOT NULL,
                              is_active BOOLEAN NOT NULL DEFAULT TRUE,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ROLE (
                      id SERIAL PRIMARY KEY,
                      name VARCHAR(50) NOT NULL UNIQUE,
                      description TEXT
);

CREATE TABLE IF NOT EXISTS USER_ROLE (
                           user_id INT NOT NULL,
                           role_id INT NOT NULL,
                           assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (user_id, role_id),
                           CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES USER_ACCOUNT(id) ON DELETE CASCADE,
                           CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES ROLE(id) ON DELETE CASCADE
);

-- NUEVA TABLA: PERMISSION (Catálogo de permisos de grano fino)
CREATE TABLE IF NOT EXISTS PERMISSION (
                            id SERIAL PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE, -- Ej: TICKET_CREATE, TICKET_ASSIGN, COMMENT_ADD
                            description TEXT
);

-- NUEVA TABLA INTERMEDIA: ROLE_PERMISSION (Relación N:M entre ROLE y PERMISSION)
CREATE TABLE IF NOT EXISTS ROLE_PERMISSION (
                                 role_id INT NOT NULL,
                                 permission_id INT NOT NULL,
                                 assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                 PRIMARY KEY (role_id, permission_id),
                                 CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES ROLE(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_role_permission_perm FOREIGN KEY (permission_id) REFERENCES PERMISSION(id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 2. CATÁLOGOS DEL SISTEMA (RF-03)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS CATEGORY (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL UNIQUE,
                          description TEXT,
                          is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS PRIORITY (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL UNIQUE,
                          description TEXT,
                          level INT NOT NULL UNIQUE CHECK (level > 0)
);

CREATE TABLE IF NOT EXISTS TICKET_STATUS (
                               id SERIAL PRIMARY KEY,
                               name VARCHAR(50) NOT NULL UNIQUE,
                               description TEXT,
                               is_final BOOLEAN NOT NULL DEFAULT FALSE
);

-- -----------------------------------------------------------------------------
-- 3. REGLAS DE FLUJO Y TRANSICIONES (RF-04)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS STATUS_TRANSITION (
                                   id SERIAL PRIMARY KEY,
                                   from_status_id INT NOT NULL,
                                   to_status_id INT NOT NULL,
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                   CONSTRAINT fk_transition_from FOREIGN KEY (from_status_id) REFERENCES TICKET_STATUS(id),
                                   CONSTRAINT fk_transition_to FOREIGN KEY (to_status_id) REFERENCES TICKET_STATUS(id),
                                   CONSTRAINT uk_status_transition UNIQUE (from_status_id, to_status_id)
);

-- -----------------------------------------------------------------------------
-- 4. ENTIDAD PRINCIPAL: TICKET (RF-03)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS TICKET (
                        id SERIAL PRIMARY KEY,
                        code VARCHAR(20) NOT NULL UNIQUE,
                        title VARCHAR(150) NOT NULL,
                        description TEXT NOT NULL,
                        category_id INT NOT NULL,
                        priority_id INT NOT NULL,
                        status_id INT NOT NULL,
                        requester_id INT NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT fk_ticket_category FOREIGN KEY (category_id) REFERENCES CATEGORY(id),
                        CONSTRAINT fk_ticket_priority FOREIGN KEY (priority_id) REFERENCES PRIORITY(id),
                        CONSTRAINT fk_ticket_status FOREIGN KEY (status_id) REFERENCES TICKET_STATUS(id),
                        CONSTRAINT fk_ticket_requester FOREIGN KEY (requester_id) REFERENCES USER_ACCOUNT(id)
);

-- -----------------------------------------------------------------------------
-- 5. ASIGNACIÓN DE TÉCNICOS (RF-01, RF-04)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS TICKET_ASSIGNMENT (
                                   id SERIAL PRIMARY KEY,
                                   ticket_id INT NOT NULL,
                                   technician_id INT NOT NULL,
                                   assigned_by INT NOT NULL,
                                   assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   unassigned_at TIMESTAMP WITH TIME ZONE,
                                   CONSTRAINT fk_assignment_ticket FOREIGN KEY (ticket_id) REFERENCES TICKET(id) ON DELETE CASCADE,
                                   CONSTRAINT fk_assignment_technician FOREIGN KEY (technician_id) REFERENCES USER_ACCOUNT(id),
                                   CONSTRAINT fk_assignment_assigned_by FOREIGN KEY (assigned_by) REFERENCES USER_ACCOUNT(id),
                                   CONSTRAINT chk_assignment_dates CHECK (unassigned_at IS NULL OR unassigned_at >= assigned_at)
);

-- Un solo técnico activo por ticket a la vez
CREATE UNIQUE INDEX idx_single_active_assignment
    ON TICKET_ASSIGNMENT (ticket_id)
    WHERE unassigned_at IS NULL;

-- -----------------------------------------------------------------------------
-- 6. COMENTARIOS E HISTORIAL (RF-05, RF-06)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS COMMENT (
                         id SERIAL PRIMARY KEY,
                         ticket_id INT NOT NULL,
                         author_id INT NOT NULL,
                         content TEXT NOT NULL,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES TICKET(id) ON DELETE CASCADE,
                         CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES USER_ACCOUNT(id)
);

CREATE TABLE IF NOT EXISTS TICKET_HISTORY (
                                id SERIAL PRIMARY KEY,
                                ticket_id INT NOT NULL,
                                previous_status_id INT NULL,
                                new_status_id INT NOT NULL,
                                changed_by INT NOT NULL,
                                changed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                CONSTRAINT fk_history_ticket FOREIGN KEY (ticket_id) REFERENCES TICKET(id) ON DELETE CASCADE,
                                CONSTRAINT fk_history_prev_status FOREIGN KEY (previous_status_id) REFERENCES TICKET_STATUS(id),
                                CONSTRAINT fk_history_new_status FOREIGN KEY (new_status_id) REFERENCES TICKET_STATUS(id),
                                CONSTRAINT fk_history_changed_by FOREIGN KEY (changed_by) REFERENCES USER_ACCOUNT(id)
);

-- -----------------------------------------------------------------------------
-- 7. ÍNDICES DE RENDIMIENTO (RF-07, RF-08)
-- -----------------------------------------------------------------------------

CREATE INDEX idx_ticket_status ON TICKET(status_id);
CREATE INDEX idx_ticket_category ON TICKET(category_id);
CREATE INDEX idx_ticket_priority ON TICKET(priority_id);
CREATE INDEX idx_ticket_requester ON TICKET(requester_id);
CREATE INDEX idx_comment_ticket ON COMMENT(ticket_id);
CREATE INDEX idx_history_ticket ON TICKET_HISTORY(ticket_id);

-- -----------------------------------------------------------------------------
-- 8. POBLADO DE DATOS INICIALES Y ASIGNACIÓN DE PERMISOS
-- -----------------------------------------------------------------------------

-- Roles
INSERT INTO ROLE (name, description) VALUES
                                         ('ADMIN', 'Administrador del sistema con control total'),
                                         ('TECHNICIAN', 'Técnico encargado del soporte y resolución'),
                                         ('USER', 'Usuario colaborador solicitante');

-- Permisos del Sistema
INSERT INTO PERMISSION (name, description) VALUES
                                               ('TICKET_CREATE', 'Permite crear nuevos tickets'),
                                               ('TICKET_READ_OWN', 'Permite ver los tickets propios'),
                                               ('TICKET_READ_ALL', 'Permite ver todos los tickets del sistema'),
                                               ('TICKET_ASSIGN', 'Permite asignar y reasignar técnicos a los tickets'),
                                               ('TICKET_CHANGE_STATUS', 'Permite cambiar el estado de un ticket'),
                                               ('COMMENT_ADD', 'Permite agregar comentarios en los tickets'),
                                               ('METRICS_VIEW', 'Permite ver el panel de indicadores y métricas generales');

-- Mapeo de Permisos a Roles (ROLE_PERMISSION)
-- ADMIN: Todos los permisos
INSERT INTO ROLE_PERMISSION (role_id, permission_id)
SELECT (SELECT id FROM ROLE WHERE name = 'ADMIN'), id FROM PERMISSION;

-- TECHNICIAN: Permisos de lectura general, cambio de estado y comentarios
INSERT INTO ROLE_PERMISSION (role_id, permission_id)
SELECT (SELECT id FROM ROLE WHERE name = 'TECHNICIAN'), id FROM PERMISSION
WHERE name IN ('TICKET_READ_ALL', 'TICKET_CHANGE_STATUS', 'COMMENT_ADD');

-- USER: Permisos de creación, lectura de sus propios tickets, cierre y comentarios
INSERT INTO ROLE_PERMISSION (role_id, permission_id)
SELECT (SELECT id FROM ROLE WHERE name = 'USER'), id FROM PERMISSION
WHERE name IN ('TICKET_CREATE', 'TICKET_READ_OWN', 'TICKET_CHANGE_STATUS', 'COMMENT_ADD');

-- Catálogos base
INSERT INTO CATEGORY (name, description) VALUES
                                             ('HARDWARE', 'Problemas de hardware'),
                                             ('SOFTWARE', 'Inconvenientes de software'),
                                             ('REDES', 'Problemas de conectividad'),
                                             ('ACCESOS', 'Gestión de credenciales'),
                                             ('OTROS', 'Otras solicitudes');

INSERT INTO PRIORITY (name, description, level) VALUES
                                                    ('BAJA', 'Atención flexible', 1),
                                                    ('MEDIA', 'Atención normal', 2),
                                                    ('ALTA', 'Atención prioritaria', 3),
                                                    ('CRITICA', 'Atención inmediata', 4);

INSERT INTO TICKET_STATUS (name, description, is_final) VALUES
                                                            ('ABIERTA', 'Creado por usuario', FALSE),
                                                            ('ASIGNADA', 'Técnico asignado', FALSE),
                                                            ('EN_PROCESO', 'En resolución', FALSE),
                                                            ('RESUELTA', 'Resuelto por técnico', FALSE),
                                                            ('CERRADA', 'Confirmado por usuario', TRUE);

INSERT INTO STATUS_TRANSITION (from_status_id, to_status_id) VALUES
                                                                 ((SELECT id FROM TICKET_STATUS WHERE name = 'ABIERTA'), (SELECT id FROM TICKET_STATUS WHERE name = 'ASIGNADA')),
                                                                 ((SELECT id FROM TICKET_STATUS WHERE name = 'ASIGNADA'), (SELECT id FROM TICKET_STATUS WHERE name = 'EN_PROCESO')),
                                                                 ((SELECT id FROM TICKET_STATUS WHERE name = 'EN_PROCESO'), (SELECT id FROM TICKET_STATUS WHERE name = 'RESUELTA')),
                                                                 ((SELECT id FROM TICKET_STATUS WHERE name = 'RESUELTA'), (SELECT id FROM TICKET_STATUS WHERE name = 'CERRADA'));