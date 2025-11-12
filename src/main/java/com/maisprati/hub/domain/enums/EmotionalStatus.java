package com.maisprati.hub.domain.enums;

/**
 * Representa o estado emocional do aluno.
 *
 * <p>Esta feature foi criada para melhorar a acessibilidade de alunos autistas,
 * permitindo que comuniquem seu estado emocional ao professor de forma não-verbal,
 * facilitando o entendimento e suporte adequado durante as atividades.</p>
 *
 * <p>O status emocional é opcional e pode ser atualizado a qualquer momento pelo aluno.</p>
 */

public enum EmotionalStatus {
    /**
     * Está tudo bem, sem preocupações
     */
    CALM,
    /**
     * Feliz e motivado com o trabalho
     */
    HAPPY,
    /**
     * Sentindo ansiedade ou estresse
     */
    ANXIOUS,
    /**
     * Confuso sobre o que fazer
     */
    CONFUSED,
    /**
     * Perdido, não sabe como proceder
     */
    LOST,
    /**
     * Frustrado ou com raiva
     */
    ANGRY,
    /**
     * Triste ou desmotivado
     */
    SAD,
    /**
     * Sobrecarregado com muitas tarefas
     */
    OVERWHELMED,
    /**
     * Focado e produtivo
     */
    FOCUSED;
}
