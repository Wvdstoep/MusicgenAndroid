package com.goal.aicontent.functions

object GenreMappings {

    val genres = mapOf("Pop Music" to listOf(
        "Create a catchy and upbeat chorus with a memorable hook.",
        "Generate a verse with a modern pop sound and relatable lyrics.",
        "Provide a danceable drum pattern for a pop party anthem.",
        "Design a synth riff that’s both energetic and infectious.",
        "Generate a bridge that builds anticipation and leads to an explosive final chorus."
    ),
        "Classical Music" to listOf(
            "Generate a melancholic and emotive violin melody in a minor key.",
            "Provide a regal and majestic brass section for a grand orchestral piece.",
            "Create a delicate and serene piano introduction for a sonata.",
            "Design a contrapuntal melody for a chamber music ensemble.",
            "Generate a hauntingly beautiful choir arrangement for a choral composition."
        ),
        "Hip-Hop/Rap" to listOf(
            "Create a head-nodding beat with a deep bassline for a rap freestyle.",
            "Generate a catchy hook with rhymes that flow seamlessly together.",
            "Provide a trap-style drum pattern with hi-hats that add rhythm and groove.",
            "Design a verse with confident and clever wordplay.",
            "Generate a chorus that incorporates auto-tuned vocals for a melodic rap track."
        ),
        "Rock Music" to listOf(
            "Create a powerful guitar riff that sets the tone for a rock anthem.",
            "Generate a hard-hitting drum pattern with driving rhythms for a rock ballad.",
            "Provide a dynamic and intense build-up for a guitar solo.",
            "Design a gritty and raw vocal melody for a rock song with attitude.",
            "Generate a bridge that transitions smoothly from a soft verse to a loud chorus."
        ),
        "Electronic/EDM" to listOf(
            "Create an infectious and energetic synth melody for a club banger.",
            "Generate a drop that unleashes a burst of energy with pounding bass and synths.",
            "Provide a build-up that elevates the anticipation before a euphoric chorus.",
            "Design a glitchy and experimental intro for an electronic track.",
            "Generate a vocal chop sequence that adds a unique twist to the composition."
        ),
        "Jazz" to listOf(
            "Craft a smooth saxophone solo with improvisational flair, capturing the essence of a smoky jazz club.",
            "Compose a walking bass line that swings, becoming the heartbeat of a quintessential jazz ensemble.",
            "Arrange a complex drum rhythm with brush strokes and syncopated beats, embodying the spirit of bebop jazz.",
            "Write a piano riff that weaves through modal changes, paying homage to the legends of jazz improvisation.",
            "Develop a trumpet solo that tells a story with every note, reminiscent of the pioneers of cool jazz."
        ),
        "Country" to listOf(
            "Compose a guitar intro with a heartfelt twang, setting the scene for a tale of American heartlands.",
            "Write a chorus that carries the melody on the wings of country harmonies, echoing the expanse of rural landscapes.",
            "Craft a fiddle solo that's as lively as a barn dance under a moonlit sky, stirring the soul with rustic warmth.",
            "Develop a bass pattern that trots along like a horse on a dusty trail, anchoring the soul of country music.",
            "Structure a bridge that weaves a narrative of heartache and hope, leading to a climax that's as cathartic as a country ballad's resolution."
        ),
        "Sea Shanty" to listOf(
            "Compose an accordion-driven melody reminiscent of old sea shanties, evoking the spirit of sailors working in unison as they raise the sails and brave the waves.",
            "Write lyrics that tell the story of a daring voyage across the seven seas, filled with tales of adventure, hardship, and camaraderie among the crew.",
            "Craft a rousing chorus that captures the rhythm and cadence of traditional sea shanties, inviting listeners to join in the call-and-response chants of sailors at work.",
            "Develop a fiddle solo that weaves through the melody like a sailor's yarn, adding layers of emotion and depth to the tale of life on the open ocean.",
            "Structure a bridge that builds upon the themes of perseverance and resilience, leading to a triumphant climax that echoes the enduring spirit of seafaring folk."
        ),
        "Reggae" to listOf(
            "Compose a guitar strumming pattern that’s as relaxed and rhythmic as a beachside jam session in Jamaica.",
            "Craft a bass line that pulses like the heartbeat of reggae, deep and melodic, invoking the spirit of the island.",
            "Arrange a drum pattern that features the offbeat magic of reggae, capturing the genre's laid-back yet vibrant essence.",
            "Write a keyboard riff that brings sunshine to the melody, infusing the track with the uplifting vibes of reggae.",
            "Develop vocal lines that speak of unity and peace, resonating with the social consciousness at the heart of reggae."
        ),
        "Blues" to listOf(
            "Craft a guitar solo that weaves a tale of sorrow and redemption, embodying the soulful depths of the blues.",
            "Compose a drum rhythm that shuffles and grooves, laying down the gritty foundation of a blues juke joint.",
            "Develop a bass line that walks with the slow, deliberate steps of a soul carrying the blues.",
            "Write a harmonica piece that cries out with the raw emotion of life's trials, echoing the voices of blues legends.",
            "Create lyrics that narrate stories of struggle and resilience, set against the backdrop of the enduring twelve-bar blues progression."
        ),
        "Folk" to listOf(
            "Create a warm and inviting acoustic guitar intro that sets a storytelling atmosphere.",
            "Generate a melody that features vocal harmonies and evokes a sense of community.",
            "Provide a simple yet profound banjo riff that adds a rustic charm.",
            "Design a lyrical narrative that captures the essence of folk tales and personal journeys.",
            "Generate a cello line that adds depth and emotional resonance to the composition."
        ),
        "R&B" to listOf(
            "Create a smooth and sultry beat that sets the stage for heartfelt vocals.",
            "Generate a bass line that's both groove-heavy and subtle, supporting the vocal melody.",
            "Provide a keyboard arrangement that blends classic soul with modern R&B vibes.",
            "Design a vocal melody that showcases range and emotion, with intricate runs and harmonies.",
            "Generate a bridge that deepens the song's emotional impact, leading to a powerful climax."
        ),
        "Metal" to listOf(
            "Create an intense and fast-paced guitar riff that drives the song with aggression.",
            "Generate a drum pattern that features double bass pedal work for added heaviness.",
            "Provide a bass guitar line that matches the intensity of the guitars while maintaining clarity.",
            "Design a vocal line that ranges from powerful screams to melodic singing.",
            "Generate a breakdown that adds tension and release, culminating in a climactic return."
        ),
        "Indie" to listOf(
            "Create a quirky and catchy guitar riff that defines the song's unique character.",
            "Generate a drum beat that's both inventive and simplistic, supporting an indie vibe.",
            "Provide a bass line that's melodic and integral to the song's hook.",
            "Design a vocal melody that conveys authenticity and emotional honesty.",
            "Generate a synth or keyboard line that adds a layer of whimsy or melancholy."
        ),
        "World Music" to listOf(
            "Create a melody that incorporates traditional instruments from non-Western cultures.",
            "Generate a rhythm that blends different cultural beats into a cohesive global fusion.",
            "Provide an instrumental solo that showcases the unique timbre of a traditional instrument.",
            "Design a song structure that reflects the musical traditions of a specific culture.",
            "Generate vocals that either adhere to traditional styles or combine various cultural influences."
        ),
        "Accordion" to listOf(
            "Compose an accordion-driven melody reminiscent of old sea shanties, evoking the spirit of sailors working in unison as they raise the sails and brave the waves.",
            "Craft a lively accordion riff that captures the rollicking energy of a sea voyage, infusing the melody with the spirit of adventure and camaraderie."
        ),
        "Fiddle" to listOf(
            "Develop a fiddle solo that weaves through the melody like a sailor's yarn, adding layers of emotion and depth to the tale of life on the open ocean.",
            "Write a fiddle melody that dances atop the waves, echoing the joy and sorrow of sailors' tales told around the ship's deck under a starry sky."
        ),
        "Guitar" to listOf(
            "Compose a guitar intro with a heartfelt twang, setting the scene for a tale of high seas and salty air, where every chord resonates with the spirit of maritime adventure.",
            "Craft a guitar accompaniment that strums like the rhythmic pulse of ocean waves, carrying the melody forward with the steady determination of a sailor at the helm."
        ),
        "Banjo" to listOf(
            "Write a banjo riff that conjures images of sailors gathered 'round a flickering lantern, regaling one another with tales of distant lands and perilous journeys.",
            "Develop a lively banjo melody that gallops across the song like a swift ship cutting through the waves, infusing the music with the infectious energy of a sea shanty."
        ),
        "Mandolin" to listOf(
            "Craft a mandolin interlude that sails through the melody like a seagull skimming the ocean's surface, adding a touch of whimsy and charm to the song's maritime theme.",
            "Compose a haunting mandolin melody that echoes across the sea, evoking the longing and nostalgia of sailors yearning for home as they chart their course through uncharted waters."
        ),
        "Concertina" to listOf(
            "Develop a concertina riff that whirls and twirls like a gust of wind catching the sails, infusing the song with the spirited energy of a crew working in harmony.",
            "Write a concertina accompaniment that dances lightly across the melody, conjuring visions of sailors' feet tapping to the rhythm as they work together to navigate the treacherous sea."
        ),
        "Ambient" to listOf(
            "Create a soundscape that evokes a sense of space and serenity, using layered synthesizers.",
            "Generate a slow-evolving melody that emphasizes texture over rhythm.",
            "Provide a minimalist piano pattern that adds a touch of melody within the ambient wash.",
            "Design an atmospheric buildup that uses field recordings and natural sounds.",
            "Generate a composition that focuses on tranquility and the subtle interplay of ambient noises."
        )
    )

}