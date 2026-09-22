package br.com.senac.projectdevalle.platform.domain;

// Configuração única da plataforma (sempre existe: criada pela migration com valores iniciais).
public interface PlatformSettingsRepository {

    PlatformSettings get();

    PlatformSettings save(PlatformSettings settings);
}
