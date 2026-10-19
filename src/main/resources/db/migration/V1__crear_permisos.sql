-- Qué puede hacer cada usuario sobre cada snippet.
CREATE TABLE permissions (
    id         BIGSERIAL    PRIMARY KEY,
    snippet_id UUID         NOT NULL, -- id del snippet, lo genera Snippet Service
    user_id    VARCHAR(255) NOT NULL, -- id del usuario en Auth0
    type       VARCHAR(10)  NOT NULL CHECK (type IN ('OWNER', 'LECTURA')),
    UNIQUE (snippet_id, user_id) -- un usuario tiene un solo permiso por snippet
);

-- Un snippet tiene un solo owner.
CREATE UNIQUE INDEX permissions_un_owner_por_snippet ON permissions (snippet_id) WHERE type = 'OWNER';
